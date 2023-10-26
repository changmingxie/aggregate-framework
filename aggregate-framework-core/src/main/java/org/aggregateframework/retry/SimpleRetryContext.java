package org.aggregateframework.retry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by changming.xie on 2/2/16.
 */
public class SimpleRetryContext implements RetryContext {

    private volatile AtomicInteger count = new AtomicInteger(0);

    private volatile Throwable lastException;

    @Override
    public int getRetryCount() {
        return count.get();
    }

    @Override
    public Throwable getLastThrowable() {
        return lastException;
    }

    @Override
    public void registerThrowable(Throwable throwable) {
        this.lastException = throwable;
        if (throwable != null)
            count.incrementAndGet();
    }
}
