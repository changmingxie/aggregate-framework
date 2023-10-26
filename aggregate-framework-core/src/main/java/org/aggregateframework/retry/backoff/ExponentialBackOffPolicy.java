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

/**
 * Created by changming.xie on 2/1/16.
 * part of the source code come from open source:spring-retry.
 */
public class ExponentialBackOffPolicy implements SleepingBackOffPolicy<ExponentialBackOffPolicy> {

    /**
     * The default 'initialInterval' value - 100 millisecs. Coupled with the
     * default 'multiplier' value this gives a useful initial spread of pauses
     * for 1-5 retries.
     */
    public static final long DEFAULT_INITIAL_INTERVAL = 100L;

    /**
     * The default maximum backoff time (30 seconds).
     */
    public static final long DEFAULT_MAX_INTERVAL = 30000L;

    /**
     * The default 'multiplier' value - value 2 (100% increase per backoff).
     */
    public static final double DEFAULT_MULTIPLIER = 2;

    /**
     * The initial sleep interval.
     */
    private volatile long initialInterval = DEFAULT_INITIAL_INTERVAL;

    /**
     * The maximum value of the backoff period in milliseconds.
     */
    private volatile long maxInterval = DEFAULT_MAX_INTERVAL;

    /**
     * The value to increment the exp seed with for each retry attempt.
     */
    private volatile double multiplier = DEFAULT_MULTIPLIER;

    private Sleeper sleeper = new ThreadWaitSleeper();

    /**
     * Public setter for the {@link Sleeper} strategy.
     *
     * @param sleeper the sleeper to set defaults to {@link ThreadWaitSleeper}.
     */
    public void setSleeper(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    public ExponentialBackOffPolicy withSleeper(Sleeper sleeper) {
        ExponentialBackOffPolicy res = newInstance();
        cloneValues(res);
        res.setSleeper(sleeper);
        return res;
    }

    protected ExponentialBackOffPolicy newInstance() {
        return new ExponentialBackOffPolicy();
    }

    protected void cloneValues(ExponentialBackOffPolicy target) {
        target.setInitialInterval(getInitialInterval());
        target.setMaxInterval(getMaxInterval());
        target.setMultiplier(getMultiplier());
        target.setSleeper(sleeper);
    }

    /**
     * The initial period to sleep on the first backoff.
     *
     * @return the initial interval
     */
    public long getInitialInterval() {
        return initialInterval;
    }

    /**
     * Set the initial sleep interval value. Default is <code>100</code>
     * millisecond. Cannot be set to a value less than one.
     */
    public void setInitialInterval(long initialInterval) {
        this.initialInterval = (initialInterval > 1 ? initialInterval : 1);
    }

    /**
     * The maximum interval to sleep for. Defaults to 30 seconds.
     *
     * @return the maximum interval.
     */
    public long getMaxInterval() {
        return maxInterval;
    }

    /**
     * Setter for maximum back off period. Default is 30000 (30 seconds). the
     * value will be reset to 1 if this method is called with a value less than
     * 1. Set this to avoid infinite waits if backing off a large number of
     * times (or if the multiplier is set too high).
     *
     * @param maxInterval in milliseconds.
     */
    public void setMaxInterval(long maxInterval) {
        this.maxInterval = maxInterval > 0 ? maxInterval : 1;
    }

    /**
     * The multiplier to use to generate the next backoff interval from the
     * last.
     *
     * @return the multiplier in use
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Set the multiplier value. Default is '<code>2.0</code>'. Hint: do not use
     * values much in excess of 1.0 (or the backoff will get very long very
     * fast).
     */
    public void setMultiplier(double multiplier) {
        this.multiplier = (multiplier > 1.0 ? multiplier : 1.0);
    }

    /**
     * Returns a new instance of {@link BackOffContext} configured with the
     * 'expSeed' and 'increment' values.
     */
    public BackOffContext start(RetryContext context) {
        return new ExponentialBackOffContext(this.initialInterval, this.multiplier, this.maxInterval);
    }

    /**
     * Pause for a length of time equal to '
     * <code>exp(backOffContext.expSeed)</code>'.
     */
    public void backOff(BackOffContext backOffContext) {
        ExponentialBackOffContext context = (ExponentialBackOffContext) backOffContext;
        try {
            long sleepTime = context.getSleepAndIncrement();

            sleeper.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new Error("Thread interrupted while sleeping", e);
        }
    }

    static class ExponentialBackOffContext implements BackOffContext {

        private final double multiplier;

        private long interval;

        private long maxInterval;

        public ExponentialBackOffContext(long expSeed, double multiplier, long maxInterval) {
            this.interval = expSeed;
            this.multiplier = multiplier;
            this.maxInterval = maxInterval;
        }

        public synchronized long getSleepAndIncrement() {
            long sleep = this.interval;
            if (sleep > maxInterval) {
                sleep = maxInterval;
            } else {
                this.interval = getNextInterval();
            }
            return sleep;
        }

        protected long getNextInterval() {
            return (long) (this.interval * this.multiplier);
        }

        public double getMultiplier() {
            return multiplier;
        }

        public long getInterval() {
            return interval;
        }

        public long getMaxInterval() {
            return maxInterval;
        }
    }

}
