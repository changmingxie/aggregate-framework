package org.aggregateframework.eventhandling.processor;

import org.aggregateframework.SystemException;
import org.aggregateframework.eventhandling.annotation.Retryable;
import org.aggregateframework.eventhandling.processor.retry.*;
import org.apache.commons.lang3.StringUtils;

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

    public void invoke(final Method method, final Object target, final Object... params) {
        final Retryable retryable = method.getAnnotation(Retryable.class);

        try {
            if (retryable == null) {
                method.invoke(target, params);
            } else {

                final PolicyBuilder policyBuilder = new PolicyBuilder();
                RetryTemplate retryTemplate = new RetryTemplate();

                RetryPolicy retryPolicy = policyBuilder.getRetryPolicy(retryable);
                retryTemplate.setRetryPolicy(retryPolicy);
                retryTemplate.setBackOffPolicy(policyBuilder.getBackoffPolicy(retryable.backoff()));

                RetryContext retryContext = retryPolicy.requireRetryContext();

                RetryCallback<Object, Exception> retryCallback = new RetryCallback<Object, Exception>() {
                    @Override
                    public Object doWithRetry(RetryContext context) throws Exception {
                        return method.invoke(target, params);
                    }
                };

                RecoveryCallback<Object> recoveryCallback = null;

                if (StringUtils.isNotEmpty(retryable.recoverMethod())) {
                    recoveryCallback = new RecoveryCallback<Object>() {

                        @Override
                        public Object recover(RetryContext context) {
                            try {
                                Method recoverMethod = target.getClass().getMethod(retryable.recoverMethod(), method.getParameterTypes());
                                return recoverMethod.invoke(target, params);
                            } catch (Exception ex) {
                                throw new SystemException(ex);
                            }
                        }
                    };
                }

                retryTemplate.execute(retryContext, retryCallback, recoveryCallback);
            }
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }
}
