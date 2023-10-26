package org.aggregateframework.processor;

import io.netty.channel.ChannelHandlerContext;
import org.aggregateframework.constants.RemotingServiceCode;
import org.aggregateframework.recovery.RecoveryExecutor;
import org.aggregateframework.remoting.RequestProcessor;
import org.aggregateframework.remoting.protocol.RemotingCommand;
import org.aggregateframework.transaction.serializer.TransactionStoreSerializer;

public class ClientRecoveryProcessor implements RequestProcessor<ChannelHandlerContext> {

    private RecoveryExecutor recoveryExecutor;
    private TransactionStoreSerializer serializer;

    public ClientRecoveryProcessor(TransactionStoreSerializer serializer, RecoveryExecutor recoveryExecutor) {
        this.serializer = serializer;
        this.recoveryExecutor = recoveryExecutor;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {


        switch (request.getServiceCode()) {
            case RemotingServiceCode.RECOVER:
                recoveryExecutor.recover(serializer.deserialize(request.getBody()));
                return RemotingCommand.createServiceResponseCommand(null);
            case RemotingServiceCode.DESERIALIZE_TRANSACTION:
                return processRequestForFindDeserializedTransactionStore(request);
        }
        return RemotingCommand.createServiceResponseCommand(null);
    }

    private RemotingCommand processRequestForFindDeserializedTransactionStore(RemotingCommand request) {
        RemotingCommand responseCommand = RemotingCommand.createServiceResponseCommand(null);
        responseCommand.setBody(recoveryExecutor.transactionVisualize(null, request.getBody()));
        return responseCommand;
    }
}
