package org.aggregateframework.ignite.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Created by changming.xie on 11/16/16.
 */
public final class CacheStoreTransactionManager {

//    private ThreadLocal<Long> storeWriteCount = new ThreadLocal<Long>();

    private ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<TransactionStatus>();

    private PlatformTransactionManager platformTransactionManager;

    public void ensureBegin() {

        if (transactionStatus.get() == null) {
            transactionStatus.set(platformTransactionManager.getTransaction(new DefaultTransactionDefinition()));
        }

    }

    public void commit(boolean commit) {

        if (transactionStatus.get() == null) {
            return;
        }

        try {
            if (commit) {
                platformTransactionManager.commit(transactionStatus.get());
            } else {
                transactionStatus.get().setRollbackOnly();
                platformTransactionManager.rollback(transactionStatus.get());
            }
        } finally {
            transactionStatus.set(null);
        }
    }

    public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }
}
