package org.aggregateframework;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.aggregateframework.constants.MixAll;
import org.aggregateframework.exception.SystemException;
import org.aggregateframework.discovery.registry.RegistryFactory;
import org.aggregateframework.discovery.registry.RegistryService;
import org.aggregateframework.monitor.ServerFlowMonitor;
import org.aggregateframework.processor.ServerRecoveryExecutor;
import org.aggregateframework.processor.ServerRequestProcessor;
import org.aggregateframework.recovery.RecoveryExecutor;
import org.aggregateframework.recovery.RecoveryScheduler;
import org.aggregateframework.recovery.TransactionStoreRecovery;
import org.aggregateframework.remoting.RemotingServer;
import org.aggregateframework.remoting.RequestProcessor;
import org.aggregateframework.remoting.netty.ChannelGroupMap;
import org.aggregateframework.remoting.netty.NettyRemotingServer;
import org.aggregateframework.support.FactoryBuilder;
import org.aggregateframework.transaction.serializer.RemotingCommandSerializer;
import org.aggregateframework.transaction.serializer.DefaultRemotingCommandSerializer;
import org.aggregateframework.transaction.serializer.DefaultTransactionStoreSerializer;
import org.aggregateframework.transaction.serializer.TransactionStoreSerializer;
import org.aggregateframework.storage.StorageType;
import org.aggregateframework.storage.TransactionStorage;
import org.aggregateframework.storage.TransactionStorageFactory;
import org.aggregateframework.utils.NetUtils;
import org.aggregateframework.utils.StopUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AggServer implements AggService {

    static final Logger logger = LoggerFactory.getLogger(AggServer.class.getSimpleName());

    private ServerConfig serverConfig = ServerConfig.DEFAULT;

    private TransactionStorage transactionStorage;

    private RecoveryExecutor recoveryExecutor;

    private RequestProcessor requestProcessor;

    private TransactionStoreRecovery transactionStoreRecovery;

    private ExecutorService requestProcessExecutor;

    private TransactionStoreSerializer transactionStoreSerializer;

    private RemotingCommandSerializer remotingCommandSerializer;

    private volatile boolean isShutdown = false;

    private RecoveryScheduler scheduler;

    private RemotingServer remotingServer;

    private RegistryService registryService;

    public AggServer(ServerConfig serverConfig) {
        if (serverConfig != null) {
            this.serverConfig = serverConfig;
        }

        this.transactionStoreSerializer = new DefaultTransactionStoreSerializer();
        this.remotingCommandSerializer = new DefaultRemotingCommandSerializer();

        if (this.serverConfig.getStorageType() == StorageType.REMOTING) {
            throw new SystemException(String.format("unsupported StorageType<%s> in server side.", this.serverConfig.getStorageType().value()));
        }

        this.remotingServer = new NettyRemotingServer(this.remotingCommandSerializer, this.serverConfig);

        this.registryService = RegistryFactory.getInstance(this.serverConfig);

        this.transactionStorage = TransactionStorageFactory.create(transactionStoreSerializer, this.serverConfig, false);

        this.scheduler = new RecoveryScheduler(this.serverConfig);

        this.recoveryExecutor = new ServerRecoveryExecutor(this.scheduler, this.transactionStoreSerializer, this.remotingServer);

        this.requestProcessor = new ServerRequestProcessor(this.scheduler, this.transactionStoreSerializer, this.transactionStorage);

        this.transactionStoreRecovery = new TransactionStoreRecovery(this.transactionStorage, this.recoveryExecutor, this.serverConfig);
    }

    @Override
    @PostConstruct
    public void start() throws Exception {
        this.isShutdown = false;
        initializeRemotingServer();

        try {
            initializeRegistry();
        } catch (Exception e) {
            logger.error("failed to initialize registryService, stop the application!", e);
            StopUtils.stop();
        }

        ServerFlowMonitor.startMonitorScheduler(this.serverConfig.getFlowMonitorPrintIntervalMinutes());
    }

    @Override
    @PreDestroy
    public void shutdown() throws Exception {

        this.isShutdown = true;

        if (this.registryService != null) {
            this.registryService.close();
        }

        if (this.remotingServer != null) {
            this.remotingServer.shutdown();
        }

        if (this.scheduler != null) {
            this.scheduler.shutdown();
        }

        if (this.requestProcessExecutor != null) {
            this.requestProcessExecutor.shutdown();
        }

        if (this.transactionStoreRecovery != null) {
            this.transactionStoreRecovery.close();
        }

        this.transactionStoreSerializer = null;

        this.recoveryExecutor = null;
        this.requestProcessor = null;
        this.transactionStorage = null;
    }

    @Override
    public TransactionStoreRecovery getTransactionStoreRecovery() {
        return transactionStoreRecovery;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    private void initializeRemotingServer() {

        this.requestProcessExecutor = new ThreadPoolExecutor(serverConfig.getRequestProcessThreadSize(),
                serverConfig.getRequestProcessThreadSize(),
                1000L * 60, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(this.serverConfig.getRequestProcessThreadQueueCapacity()),
                new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, String.format("StoreTransactionThread_%d", threadIndex.getAndIncrement()));
                    }
                });

        remotingServer.registerDefaultProcessor(this.requestProcessor, this.requestProcessExecutor);

        remotingServer.registerChannelHandlers(new UnregisterScheduleHandler());

        remotingServer.start();
    }

    private void initializeRegistry() {
        InetSocketAddress addressForClient;
        if (StringUtils.isNotEmpty(this.serverConfig.getRegistryAddress())) {
            addressForClient = NetUtils.toInetSocketAddress(this.serverConfig.getRegistryAddress());
        } else {
            addressForClient = new InetSocketAddress(NetUtils.getLocalAddress(), serverConfig.getListenPort());
        }
        InetSocketAddress addressForDashboard;
        if (StringUtils.isNotEmpty(this.serverConfig.getRegistryAddressForDashboard())) {
            addressForDashboard = NetUtils.toInetSocketAddress(this.serverConfig.getRegistryAddressForDashboard());
        } else {
            addressForDashboard = new InetSocketAddress(NetUtils.getLocalAddress(), serverConfig.getRegistryPortForDashboard());
        }
        this.registryService.start();
        this.registryService.register(addressForClient, addressForDashboard);

        logger.info("succeeded to register with address {},{}", addressForClient, addressForDashboard);
    }

    public RecoveryScheduler getScheduler() {
        return scheduler;
    }

    public TransactionStorage getTransactionStorage() {
        return transactionStorage;
    }

    public RecoveryExecutor getRecoveryExecutor() {
        return recoveryExecutor;
    }

    @ChannelHandler.Sharable
    class UnregisterScheduleHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) {

            if (!isShutdown) {

                String domain = (String) ctx.channel().attr(AttributeKey.valueOf(MixAll.DOMAIN)).get();

                if (domain != null) {

                    Set<Channel> channels = FactoryBuilder.factoryOf(ChannelGroupMap.class).getInstance().getAllChannels(domain);

                    if (channels == null) {
                        return;
                    }

                    for (Channel channel : channels) {
                        if (channel != ctx.channel()) {
                            return;
                        }
                    }

                    //no other active channels, shutdown scheduler for the domain
                    scheduler.unregisterSchedule(domain);
                }
            }
        }
    }
}
