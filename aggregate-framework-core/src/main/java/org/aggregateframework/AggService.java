package org.aggregateframework;

import org.aggregateframework.recovery.TransactionStoreRecovery;

public interface AggService {

    void start() throws Exception;

    void shutdown() throws Exception;

    TransactionStoreRecovery getTransactionStoreRecovery();
}
