package org.aggregateframework.eventhandling.processor.retry;

/**
 * Created by changming.xie on 2/2/16.
 */
public interface RetryCallback<T, E extends Throwable> {

    T doWithRetry(RetryContext context) throws E;
}
