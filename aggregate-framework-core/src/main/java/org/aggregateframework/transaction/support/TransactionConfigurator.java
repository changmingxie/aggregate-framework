package org.aggregateframework.transaction.support;


import org.aggregateframework.transaction.TransactionManager;
import org.aggregateframework.transaction.recovery.RecoverFrequency;
import org.aggregateframework.transaction.repository.TransactionRepository;

import java.util.concurrent.locks.Lock;

/**
 * Created by changming.xie on 2/24/17.
 */
public interface TransactionConfigurator {

    TransactionManager getTransactionManager();

    TransactionRepository getTransactionRepository();

    RecoverFrequency getRecoverFrequency();

    Lock getRecoveryLock();
}
