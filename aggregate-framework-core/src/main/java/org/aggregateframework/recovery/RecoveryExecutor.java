package org.aggregateframework.recovery;

import org.aggregateframework.storage.TransactionStore;

public interface RecoveryExecutor {

    void recover(TransactionStore transactionStore);

    byte[] transactionVisualize(String domain, byte[] content);
}
