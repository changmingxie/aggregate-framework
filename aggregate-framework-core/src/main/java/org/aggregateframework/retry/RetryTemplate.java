package org.aggregateframework.retry;

import org.aggregateframework.retry.backoff.BackOffContext;
import org.aggregateframework.retry.backoff.BackOffPolicy;
import org.aggregateframework.retry.backoff.NoBackOffPolicy;

import java.util.Collections;

/**
 * Created by changming.xie on 2/2/16.
 */
public class RetryTemplate implements RetryOperations {

    private volatile BackOffPolicy backOffPolicy = new NoBackOffPolicy();

    private volatile RetryPolicy retryPolicy =
            new SimpleRetryPolicy(3, Collections.<Class<? extends Throwable>, Boolean>singletonMap(Exception.class, true));


    @Override
    public <T> T execute(RetryContext context, RetryCallback<T> retryCallback) {
        return doExecute(context, retryCallback, null);
    }

    @Override
    public <T> T execute(RetryContext context, RetryCallback<T> retryCallback, RecoveryCallback<T> recoveryCallback) {
        return doExecute(context, retryCallback, recoveryCallback);
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public void setBackOffPolicy(BackOffPolicy backOffPolicy) {
        this.backOffPolicy = backOffPolicy;
    }

    protected <T> T doExecute(RetryContext context, RetryCallback<T> retryCallback,
                                                   RecoveryCallback<T> recoveryCallback) {

        RetryPolicy retryPolicy = this.retryPolicy;
        BackOffPolicy backOffPolicy = this.backOffPolicy;

        BackOffContext backOffContext = backOffPolicy.start(context);

        Throwable lastException = null;

        while (canRetry(retryPolicy, context)) {

            backOffPolicy.backOff(backOffContext);

            try {
                return retryCallback.doWithRetry(context);
            } catch (Throwable e) {
                lastException = e;
                registerThrowable(retryPolicy, context, e);
            }
        }

        return handleRetryExhausted(recoveryCallback, context, lastException);
    }

    protected boolean canRetry(RetryPolicy retryPolicy, RetryContext context) {
        return retryPolicy.canRetry(context);
    }

    protected <T> T handleRetryExhausted(RecoveryCallback<T> recoveryCallback, RetryContext context, Throwable lastException) {

        if (recoveryCallback != null) {
            return recoveryCallback.recover(context);
        }

        throw new Error(lastException);
    }

    protected void registerThrowable(RetryPolicy retryPolicy, RetryContext context, Throwable e) {
        retryPolicy.registerThrowable(context, e);
    }
}
