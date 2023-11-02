package org.aggregateframework.recovery;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aggregateframework.ServerConfig;
import org.aggregateframework.alert.AlertManager;
import org.aggregateframework.storage.*;
import org.aggregateframework.storage.domain.DomainStore;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by changmingxie on 11/10/15.
 */
public class TransactionStoreRecovery implements Closeable {

    public static final int CONCURRENT_RECOVERY_TIMEOUT = 60;

    public static final int MAX_ERROR_COUNT_SHREDHOLD = 15;

    static final Logger logger = LoggerFactory.getLogger(TransactionStoreRecovery.class.getSimpleName());
    private final AtomicInteger triggerMaxRetryPrintCount = new AtomicInteger();
    private final AtomicInteger recoveryFailedPrintCount = new AtomicInteger();
    private final Lock logSync = new ReentrantLock();
    private volatile int logMaxPrintCount;
    private TransactionStorage transactionStorage;

    private RecoveryExecutor recoveryExecutor;

    private RecoveryConfig recoveryConfig;

    private ExecutorService recoveryExecutorService;

    private ObjectMapper jackson = new ObjectMapper();

    private boolean serverMode;

    public TransactionStoreRecovery(TransactionStorage transactionStorage, RecoveryExecutor recoveryExecutor, RecoveryConfig recoveryConfig) {
        this.transactionStorage = transactionStorage;
        this.recoveryExecutor = recoveryExecutor;
        this.recoveryConfig = recoveryConfig;
        this.serverMode = recoveryConfig instanceof ServerConfig;

        recoveryExecutorService = Executors.newFixedThreadPool(recoveryConfig.getConcurrentRecoveryThreadCount());

        logMaxPrintCount = Math.min(recoveryConfig.getFetchPageSize() / 2, MAX_ERROR_COUNT_SHREDHOLD);
    }

    @Override
    public void close() {
        if (recoveryExecutorService != null) {
            recoveryExecutorService.shutdown();
        }
    }

    public void startRecover(String domain) {

        DomainStore domainStore = ((StorageRecoverable) transactionStorage).findDomain(domain);

        try {
            String offset = null;

            int totalCount = 0;
            do {

                Page<TransactionStore> page = loadErrorTransactionsByPage(domain, offset);

                if (!page.getData().isEmpty()) {
                    concurrentRecoveryErrorTransactions(page.getData(), domainStore.getMaxRetryCount());
                    offset = page.getNextOffset();
                    totalCount += page.getData().size();
                } else {
                    break;
                }
            } while (true);

            // 告警
            AlertManager.tryAlert(domain, totalCount, transactionStorage);

            logger.debug("total recovery count {} from repository:{}", totalCount, transactionStorage.getClass().getName());
        } catch (Throwable e) {
            logger.error("recovery failed from repository:{}.", transactionStorage.getClass().getName(), e);
        }
    }

    private Page<TransactionStore> loadErrorTransactionsByPage(String domain, String offset) {

        long currentTimeInMillis = Instant.now().toEpochMilli();

        return ((StorageRecoverable) transactionStorage).findAllUnmodifiedSince(domain, new Date(currentTimeInMillis - recoveryConfig.getRecoverDuration() * 1000), offset, recoveryConfig.getFetchPageSize());
    }


    private void concurrentRecoveryErrorTransactions(List<TransactionStore> transactions, int maxRetryCount) throws InterruptedException, ExecutionException {

        initLogStatistics();

        List<RecoverTask> tasks = new ArrayList<>();
        for (TransactionStore transaction : transactions) {
            tasks.add(new RecoverTask(transaction, maxRetryCount));
        }

        List<Future<Void>> futures = recoveryExecutorService.invokeAll(tasks, CONCURRENT_RECOVERY_TIMEOUT, TimeUnit.SECONDS);

        for (Future future : futures) {
            future.get();
        }
    }

    private void recoverErrorTransaction(TransactionStore transactionStore, int maxRetryCount) {

        if (transactionStore.getRetriedCount() > maxRetryCount) {

            logSync.lock();
            try {
                if (triggerMaxRetryPrintCount.get() < logMaxPrintCount) {
                    if (serverMode) {
                        logger.warn(
                                "recover failed with max retry count,will not try again. domain:{}, xid:{}, retried count:{}",
                                transactionStore.getDomain(),
                                transactionStore.getXid(),
                                transactionStore.getRetriedCount());
                    } else {
                        logger.error(
                                "recover failed with max retry count,will not try again. domain:{}, xid:{}, retried count:{}",
                                transactionStore.getDomain(),
                                transactionStore.getXid(),
                                transactionStore.getRetriedCount());
                    }
                    triggerMaxRetryPrintCount.incrementAndGet();
                } else if (triggerMaxRetryPrintCount.get() == logMaxPrintCount) {
                    if (serverMode) {
                        logger.warn("Too many transactionStore's retried count max then MaxRetryCount during one page transactions recover process , will not print errors again!");
                    } else {
                        logger.error("Too many transactionStore's retried count max then MaxRetryCount during one page transactions recover process , will not print errors again!");
                    }
                }

            } finally {
                logSync.unlock();
            }

            return;
        }

        try {
            recoveryExecutor.recover(transactionStore);
        } catch (Throwable throwable) {

            if (throwable instanceof TransactionOptimisticLockException
                    || ExceptionUtils.getRootCause(throwable) instanceof TransactionOptimisticLockException) {

                logger.warn(
                        "optimisticLockException happened while recover. txid:{}, retried count:{}",
                        transactionStore.getXid(),
                        transactionStore.getRetriedCount());
            } else {

                logSync.lock();
                try {
                    if (recoveryFailedPrintCount.get() < logMaxPrintCount) {
                        try {
                            if (serverMode) {
                                logger.warn("recover failed, txid:{}, retried count:{},transactionStore content:{}",
                                        transactionStore.getXid(),
                                        transactionStore.getRetriedCount(),
                                        jackson.writeValueAsString(transactionStore), throwable);
                            } else {
                                logger.error("recover failed, txid:{}, retried count:{},transactionStore content:{}",
                                        transactionStore.getXid(),
                                        transactionStore.getRetriedCount(),
                                        jackson.writeValueAsString(transactionStore), throwable);
                            }
                        } catch (JsonProcessingException e) {
                            logger.error("failed to serialize transactionStore {}", transactionStore.toString(), e);
                        }
                        recoveryFailedPrintCount.incrementAndGet();
                    } else if (recoveryFailedPrintCount.get() == logMaxPrintCount) {
                        if (serverMode) {
                            logger.warn("Too many transactionStore's recover error during one page transactions recover process , will not print errors again!");
                        } else {
                            logger.error("Too many transactionStore's recover error during one page transactions recover process , will not print errors again!");
                        }
                    }
                } finally {
                    logSync.unlock();
                }
            }
        }
    }

    private void initLogStatistics() {
        triggerMaxRetryPrintCount.set(0);
        recoveryFailedPrintCount.set(0);
    }

    public TransactionStorage getTransactionStorage() {
        return transactionStorage;
    }

    class RecoverTask implements Callable<Void> {

        private TransactionStore transaction;

        private int maxRetryCount;

        public RecoverTask(TransactionStore transaction, int maxRetryCount) {
            this.transaction = transaction;
            this.maxRetryCount = maxRetryCount;
        }

        @Override
        public Void call() throws Exception {
            recoverErrorTransaction(transaction, maxRetryCount);
            return null;
        }
    }
}
