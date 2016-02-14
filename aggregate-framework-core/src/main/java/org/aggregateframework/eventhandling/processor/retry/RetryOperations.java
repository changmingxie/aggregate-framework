package org.aggregateframework.eventhandling.processor.retry;

/**
 * Created by changming.xie on 2/2/16.
 */
public interface RetryOperations {

    <T, E extends Throwable> T execute(RetryContext context, RetryCallback<T, E> retryCallback) throws E;

    <T, E extends Throwable> T execute(RetryContext context, RetryCallback<T, E> retryCallback, RecoveryCallback<T> recoveryCallback) throws E;
}
