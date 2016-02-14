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

package org.aggregateframework.eventhandling.processor.backoff;

import org.aggregateframework.eventhandling.processor.retry.RetryContext;

/**
 * Created by changming.xie on 2/1/16.
 * part of the source code come from open source:spring-retry.
 */
public class FixedBackOffPolicy implements SleepingBackOffPolicy<FixedBackOffPolicy> {

    /**
     * Default back off period - 1000ms.
     */
    private static final long DEFAULT_BACK_OFF_PERIOD = 1000L;

    /**
     * The back off period in milliseconds. Defaults to 1000ms.
     */
    private volatile long backOffPeriod = DEFAULT_BACK_OFF_PERIOD;

    private Sleeper sleeper = new ThreadWaitSleeper();

    public FixedBackOffPolicy withSleeper(Sleeper sleeper) {
        FixedBackOffPolicy res = new FixedBackOffPolicy();
        res.setBackOffPeriod(backOffPeriod);
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
     * Set the back off period in milliseconds. Cannot be &lt; 1. Default value is 1000ms.
     */
    public void setBackOffPeriod(long backOffPeriod) {
        this.backOffPeriod = (backOffPeriod > 0 ? backOffPeriod : 1);
    }

    /**
     * The backoff period in milliseconds.
     *
     * @return the backoff period
     */
    public long getBackOffPeriod() {
        return backOffPeriod;
    }


    @Override
    public BackOffContext start(RetryContext context) {
        return null;
    }

    public void backOff(BackOffContext backOffContext) {
        try {
            sleeper.sleep(backOffPeriod);
        } catch (InterruptedException e) {
            throw new Error("Thread interrupted while sleeping", e);
        }
    }

    public String toString() {
        return "FixedBackOffPolicy[backOffPeriod=" + backOffPeriod + "]";
    }
}
