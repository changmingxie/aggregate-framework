package org.aggregateframework.retry;

/**
 * Created by changming.xie on 2/2/16.
 */
public interface RetryCallback<T> {

    T doWithRetry(RetryContext context);
}
