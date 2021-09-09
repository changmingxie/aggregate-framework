package org.aggregateframework.transaction.recovery;

import org.aggregateframework.transaction.TransactionManager;
import org.aggregateframework.transaction.repository.CacheableTransactionRepository;
import org.aggregateframework.transaction.repository.TransactionRepository;
import org.aggregateframework.transaction.support.TransactionConfigurator;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

/**
 * Created by changming.xie on 11/25/17.
 */
public class RecoverConfiguration implements TransactionConfigurator {


    private TransactionManager transactionManager;
    private TransactionRepository transactionRepository;
    private RecoverFrequency recoverFrequency = DefaultRecoverFrequency.INSTANCE;
    private RecoveryLock recoveryLock = RecoveryLock.DEFAULT_LOCK;

    private Scheduler scheduler;
    private String jobName;
    private String triggerName;
    private int threadCount = Runtime.getRuntime().availableProcessors();

    @PostConstruct
    public void init() throws Exception {
        transactionManager = new TransactionManager();
        transactionManager.setTransactionRepository(transactionRepository);

        if (transactionRepository instanceof CacheableTransactionRepository) {
            ((CacheableTransactionRepository) transactionRepository).setExpireDuration(recoverFrequency.getRecoverDuration());
        }

        TransactionRecovery transactionRecovery = new TransactionRecovery();
        transactionRecovery.setTransactionConfigurator(this);

        RecoverScheduledJob recoveryScheduledJob = new RecoverScheduledJob();
        recoveryScheduledJob.setJobName(StringUtils.isEmpty(jobName) ? "compensableRecoverJob" : jobName);
        recoveryScheduledJob.setTriggerName(StringUtils.isEmpty(triggerName) ? "compensableTrigger" : triggerName);

        recoveryScheduledJob.setTransactionRecovery(transactionRecovery);
        recoveryScheduledJob.setCronExpression(getRecoverFrequency().getCronExpression());

        Properties conf = new Properties();
        conf.put("org.quartz.threadPool.threadCount", String.valueOf(threadCount));
        conf.put("org.quartz.scheduler.instanceName", "recovery-quartz");

        if (scheduler == null) {
            SchedulerFactory factory = new org.quartz.impl.StdSchedulerFactory(conf);
            scheduler = factory.getScheduler();
        }

        recoveryScheduledJob.setScheduler(scheduler);
        recoveryScheduledJob.init();
    }

    @PreDestroy
    public void close() throws Exception {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    @Override
    public RecoverFrequency getRecoverFrequency() {
        return recoverFrequency;
    }

    @Override
    public Lock getRecoveryLock() {
        return this.recoveryLock;
    }

    public void setRecoverFrequency(RecoverFrequency recoverFrequency) {
        this.recoverFrequency = recoverFrequency;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void setRecoveryLock(RecoveryLock recoveryLock) {
        this.recoveryLock = recoveryLock;
    }
}
