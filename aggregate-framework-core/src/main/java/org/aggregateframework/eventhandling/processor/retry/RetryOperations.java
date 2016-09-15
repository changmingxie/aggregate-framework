package org.aggregateframework.eventhandling.processor.retry;

/**
 * Created by changming.xie on 2/2/16.
 */
public interface RetryOperations {

    <T> T execute(RetryContext context, RetryCallback<T> retryCallback);

    <T> T execute(RetryContext context, RetryCallback<T> retryCallback, RecoveryCallback<T> recoveryCallback);
}
