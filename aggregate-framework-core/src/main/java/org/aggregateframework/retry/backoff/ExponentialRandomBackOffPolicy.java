/*
 * Copyright 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aggregateframework.retry.backoff;

import org.aggregateframework.retry.RetryContext;

import java.util.Random;

/**
 * Created by changming.xie on 2/1/16.
 * part of the source code come from open source:spring-retry.
 * <p>
 * This has shown to at least be useful in testing scenarios where excessive contention is generated
 * by the test needing many retries.  In test, usually threads are started at the same time, and thus
 * stomp together onto the next interval.  Using this {@link BackOffPolicy} can help avoid that scenario.
 * <p>
 * Example:
 * initialInterval = 50
 * multiplier      = 2.0
 * maxInterval     = 3000
 * numRetries      = 5
 * <p>
 * {@link ExponentialBackOffPolicy} yields:           [50, 100, 200, 400, 800]
 * <p>
 * {@link ExponentialRandomBackOffPolicy} may yield   [50, 100, 100, 100, 600]
 * or   [50, 100, 150, 400, 800]
 */
public class ExponentialRandomBackOffPolicy extends ExponentialBackOffPolicy {

    public BackOffContext start(RetryContext context) {
        return new ExponentialRandomBackOffContext(getInitialInterval(), getMultiplier(), getMaxInterval());
    }

    protected ExponentialBackOffPolicy newInstance() {
        return new ExponentialRandomBackOffPolicy();
    }

    static class ExponentialRandomBackOffContext extends ExponentialBackOffPolicy.ExponentialBackOffContext {
        private final Random r = new Random();

        public ExponentialRandomBackOffContext(long expSeed, double multiplier, long maxInterval) {
            super(expSeed, multiplier, maxInterval);
        }

        @Override
        public synchronized long getSleepAndIncrement() {
            long next = super.getSleepAndIncrement();
            next = (long) (next * (1 + r.nextFloat() * (getMultiplier() - 1)));
            return next;
        }

    }
}
