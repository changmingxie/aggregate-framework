package org.aggregateframework.eventhandling.processor.retry;

/**
 * Created by changming.xie on 2/2/16.
 */
public interface RecoveryCallback<T> {

    T recover(RetryContext context);

}