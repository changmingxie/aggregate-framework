/*
 * Copyright 2006-2014 the original author or authors.
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
 */
public class UniformRandomBackOffPolicy implements SleepingBackOffPolicy<UniformRandomBackOffPolicy> {

    /**
     * Default min back off period - 500ms.
     */
    private static final long DEFAULT_BACK_OFF_MIN_PERIOD = 500L;

    /**
     * Default max back off period - 1500ms.
     */
    private static final long DEFAULT_BACK_OFF_MAX_PERIOD = 1500L;

    private volatile long minBackOffPeriod = DEFAULT_BACK_OFF_MIN_PERIOD;

    private volatile long maxBackOffPeriod = DEFAULT_BACK_OFF_MAX_PERIOD;

    private Random random = new Random(System.currentTimeMillis());

    private Sleeper sleeper = new ThreadWaitSleeper();

    public UniformRandomBackOffPolicy withSleeper(Sleeper sleeper) {
        UniformRandomBackOffPolicy res = new UniformRandomBackOffPolicy();
        res.setMinBackOffPeriod(minBackOffPeriod);
        res.setSleeper(sleeper);
        return res;
    }

    /**
     * Public setter for the {@link Sleeper} strategy.
     *
     * @param sleeper the sleeper to set defaults to {@link ThreadWaitSleeper}.
     */
    public void setSleeper(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    /**
     * The minimum backoff period in milliseconds.
     *
     * @return the backoff period
     */
    public long getMinBackOffPeriod() {
        return minBackOffPeriod;
    }

    /**
     * Set the minimum back off period in milliseconds. Cannot be &lt; 1. Default value
     * is 500ms.
     */
    public void setMinBackOffPeriod(long backOffPeriod) {
        this.minBackOffPeriod = (backOffPeriod > 0 ? backOffPeriod : 1);
    }

    /**
     * The maximum backoff period in milliseconds.
     *
     * @return the backoff period
     */
    public long getMaxBackOffPeriod() {
        return maxBackOffPeriod;
    }

    /**
     * Set the maximum back off period in milliseconds. Cannot be &lt; 1. Default value
     * is 1500ms.
     */
    public void setMaxBackOffPeriod(long backOffPeriod) {
        this.maxBackOffPeriod = (backOffPeriod > 0 ? backOffPeriod : 1);
    }

    public String toString() {
        return "RandomBackOffPolicy[backOffPeriod=" + minBackOffPeriod + ", " + maxBackOffPeriod + "]";
    }

    @Override
    public BackOffContext start(RetryContext context) {
        return null;
    }

    @Override
    public void backOff(BackOffContext backOffContext) {
        try {
            long delta = maxBackOffPeriod == minBackOffPeriod ? 0 : random.nextInt((int) (maxBackOffPeriod - minBackOffPeriod));
            sleeper.sleep(minBackOffPeriod + delta);
        } catch (InterruptedException e) {
            throw new Error("Thread interrupted while sleeping", e);
        }
    }
}
