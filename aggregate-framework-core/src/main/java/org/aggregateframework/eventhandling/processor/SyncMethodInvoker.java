package org.aggregateframework.eventhandling.processor;

import org.aggregateframework.SystemException;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.annotation.Retryable;
import org.aggregateframework.retry.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by changming.xie on 2/4/16.
 */
public class SyncMethodInvoker {

    private static volatile SyncMethodInvoker INSTANCE = null;

    public static SyncMethodInvoker getInstance() {

        if (INSTANCE == null) {

            synchronized (AsyncMethodInvoker.class) {

                if (INSTANCE == null) {
                    INSTANCE = new SyncMethodInvoker();
                }
            }
        }

        return INSTANCE;
    }

    public void invoke(EventInvokerEntry eventInvokerEntry) {
        final Method method = eventInvokerEntry.getMethod();
        final Object target = eventInvokerEntry.getTarget();
        final Object[] params = eventInvokerEntry.getParams();

        final Retryable retryable = method.getAnnotation(Retryable.class);

        if (retryable == null) {
            try {
                method.invoke(target, params);
            } catch (IllegalAccessException e) {
                throw new SystemException(e);
            } catch (InvocationTargetException e) {
                if (e.getCause() != null) {
                    if (e.getCause() instanceof RuntimeException) {
                        throw (RuntimeException) (e.getCause());
                    }
                }

                throw new SystemException(e);
            }
        } else {

            final PolicyBuilder policyBuilder = new PolicyBuilder();
            RetryTemplate retryTemplate = new RetryTemplate();

            RetryPolicy retryPolicy = policyBuilder.getRetryPolicy(retryable);
            retryTemplate.setRetryPolicy(retryPolicy);
            retryTemplate.setBackOffPolicy(policyBuilder.getBackoffPolicy(retryable.backoff()));

            RetryContext retryContext = retryPolicy.requireRetryContext();

            RetryCallback<Object> retryCallback = new RetryCallback<Object>() {
                @Override
                public Object doWithRetry(RetryContext context) {
                    try {
                        return method.invoke(target, params);
                    } catch (IllegalAccessException e) {
                        throw new SystemException(e);
                    } catch (InvocationTargetException e) {
                        if (e.getCause() != null) {
                            if (e.getCause() instanceof RuntimeException) {
                                throw (RuntimeException) (e.getCause());
                            }
                        }

                        throw new SystemException(e);
                    }
                }
            };

            RecoveryCallback<Object> recoveryCallback = null;

            if (StringUtils.isNotEmpty(retryable.recoverMethod())) {
                recoveryCallback = new SimpleRecoveryCallback(target, retryable.recoverMethod(), method.getParameterTypes(), params);
            }

            retryTemplate.execute(retryContext, retryCallback, recoveryCallback);

        }
    }
}
