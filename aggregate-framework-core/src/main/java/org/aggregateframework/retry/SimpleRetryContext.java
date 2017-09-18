package org.aggregateframework.retry;

/**
 * Created by changming.xie on 2/2/16.
 */
public class SimpleRetryContext implements RetryContext {

    private volatile int count;

    private volatile Throwable lastException;

    @Override
    public int getRetryCount() {
        return count;
    }

    @Override
    public Throwable getLastThrowable() {
        return lastException;
    }

    @Override
    public void registerThrowable(Throwable throwable) {
        this.lastException = throwable;
        if (throwable != null)
            count++;
    }
}
