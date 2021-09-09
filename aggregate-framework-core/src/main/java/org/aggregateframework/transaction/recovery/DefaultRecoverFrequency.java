package org.aggregateframework.transaction.recovery;

/**
 * Created by changming.xie on 6/1/16.
 */
public class DefaultRecoverFrequency implements RecoverFrequency {

    public static final RecoverFrequency INSTANCE = new DefaultRecoverFrequency();

    private int maxRetryCount = 30;

    private int recoverDuration = 30; //30 seconds

    private String cronExpression = "0/15 * * * * ? ";

    private int fetchPageSize = 500;

    private int concurrentRecoveryThreadCount = Runtime.getRuntime().availableProcessors() * 2;

    @Override
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    @Override
    public int getRecoverDuration() {
        return recoverDuration;
    }

    @Override
    public String getCronExpression() {
        return cronExpression;
    }

    @Override
    public int getConcurrentRecoveryThreadCount() {
        return concurrentRecoveryThreadCount;
    }

    public void setConcurrentRecoveryThreadCount(int concurrentRecoveryThreadCount) {
        this.concurrentRecoveryThreadCount = concurrentRecoveryThreadCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setRecoverDuration(int recoverDuration) {
        this.recoverDuration = recoverDuration;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public int getFetchPageSize() {
        return fetchPageSize;
    }

    public void setFetchPageSize(int fetchPageSize) {
        this.fetchPageSize = fetchPageSize;
    }
}
