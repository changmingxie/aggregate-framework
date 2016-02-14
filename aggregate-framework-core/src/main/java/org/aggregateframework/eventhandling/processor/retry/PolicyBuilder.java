package org.aggregateframework.eventhandling.processor.retry;

import org.aggregateframework.eventhandling.annotation.Backoff;
import org.aggregateframework.eventhandling.annotation.Retryable;
import org.aggregateframework.eventhandling.processor.backoff.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by changming.xie on 2/2/16.
 */
public class PolicyBuilder {

    public RetryPolicy getRetryPolicy(Retryable retryable) {
        Class<? extends Throwable>[] includes = retryable.value();
        if (includes.length == 0) {
            includes = retryable.include();
        }
        Class<? extends Throwable>[] excludes = retryable.exclude();
        if (includes.length == 0 && excludes.length == 0) {
            SimpleRetryPolicy simple = new SimpleRetryPolicy();
            simple.setMaxAttempts(retryable.maxAttempts());
            return simple;
        }
        Map<Class<? extends Throwable>, Boolean> policyMap = new HashMap<Class<? extends Throwable>, Boolean>();
        for (Class<? extends Throwable> type : includes) {
            policyMap.put(type, true);
        }
        for (Class<? extends Throwable> type : excludes) {
            policyMap.put(type, false);
        }
        return new SimpleRetryPolicy(retryable.maxAttempts(), policyMap, true);
    }

    public BackOffPolicy getBackoffPolicy(Backoff backoff) {
        long min = backoff.delay() == 0 ? backoff.value() : backoff.delay();
        long max = backoff.maxDelay();
        if (backoff.multiplier() > 0) {
            ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
            if (backoff.random()) {
                policy = new ExponentialRandomBackOffPolicy();
            }
            policy.setInitialInterval(min);
            policy.setMultiplier(backoff.multiplier());
            policy.setMaxInterval(max > min ? max : ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL);
            return policy;
        }
        if (max > min) {
            UniformRandomBackOffPolicy policy = new UniformRandomBackOffPolicy();
            policy.setMinBackOffPeriod(min);
            policy.setMaxBackOffPeriod(max);

            return policy;
        }
        FixedBackOffPolicy policy = new FixedBackOffPolicy();
        policy.setBackOffPeriod(min);

        return policy;
    }
}
