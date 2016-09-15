package org.aggregateframework.eventhandling.processor.retry;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by changming.xie on 2/2/16.
 */
public interface RetryCallback<T> {

    T doWithRetry(RetryContext context);
}
