package org.aggregateframework.processor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.aggregateframework.constants.RemotingServiceCode;
import org.aggregateframework.recovery.RecoveryExecutor;
import org.aggregateframework.recovery.RecoveryScheduler;
import org.aggregateframework.remoting.RemotingServer;
import org.aggregateframework.remoting.netty.ChannelGroupMap;
import org.aggregateframework.remoting.protocol.RemotingCommand;
import org.aggregateframework.remoting.protocol.RemotingCommandCode;
import org.aggregateframework.support.FactoryBuilder;
import org.aggregateframework.transaction.serializer.TransactionStoreSerializer;
import org.aggregateframework.storage.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerRecoveryExecutor implements RecoveryExecutor {

    static final Logger logger = LoggerFactory.getLogger(ServerRecoveryExecutor.class.getSimpleName());
    // the timeout for agg-server reqeust agg-client
    private static final long REQUEST_TIMEOUT_MILLIS = 2000L;
    private TransactionStoreSerializer serializer;
    private RecoveryScheduler scheduler;
    private RemotingServer remotingServer;

    public ServerRecoveryExecutor(RecoveryScheduler scheduler, TransactionStoreSerializer transactionStoreSerializer, RemotingServer remotingServer) {
        this.serializer = transactionStoreSerializer;
        this.scheduler = scheduler;
        this.remotingServer = remotingServer;
    }

    @Override
    public void recover(TransactionStore transactionStore) {
        doRecover(RemotingServiceCode.RECOVER, transactionStore);
    }

    @Override
    public byte[] transactionVisualize(String domain, byte[] content) {

        RemotingCommand requestCommand = RemotingCommand.createCommand(RemotingCommandCode.SERVICE_REQ, null);
        requestCommand.setServiceCode(RemotingServiceCode.DESERIALIZE_TRANSACTION);
        requestCommand.setBody(content);

        RemotingCommand remotingCommand = this.remotingServer.invokeSync(domain, requestCommand, REQUEST_TIMEOUT_MILLIS);

        return remotingCommand.getBody();

    }

    private void doRecover(int serviceCode, TransactionStore transactionStore) {
        RemotingCommand remotingCommand = RemotingCommand.createCommand(RemotingCommandCode.SERVICE_REQ, null);
        remotingCommand.setServiceCode(serviceCode);
        remotingCommand.setBody(serializer.serialize(transactionStore));

        Channel channel = FactoryBuilder.factoryOf(ChannelGroupMap.class).getInstance().getChannel(transactionStore.getDomain());

        if (channel == null) {
            logger.debug("no available client channels for domain<{}> to recovery", transactionStore.getDomain());
            scheduler.unregisterSchedule(transactionStore.getDomain());
            return;
        }

        try {
            channel.writeAndFlush(remotingCommand).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (!channelFuture.isSuccess()) {
                        logger.warn("send recovery command with service code {} to channel <{}> failed.", serviceCode, channel.remoteAddress());
                    }
                }
            });
        } catch (Exception e) {
            logger.warn("cannot recover", e);
        }
    }
}
