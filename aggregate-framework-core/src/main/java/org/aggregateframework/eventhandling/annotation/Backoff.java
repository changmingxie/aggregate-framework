package org.aggregateframework.eventhandling.annotation;

import java.lang.annotation.*;

/**
 * Created by changming.xie on 2/1/16.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Backoff {

    /**
     * Synonym for {@link #delay()}.
     *
     * @return the delay in milliseconds (default 1000)
     */
    long value() default 1000;

    /**
     * A canonical backoff period. Used as an initial value in the exponential case, and
     * as a minimum value in the uniform case.
     *
     * @return the initial or canonical backoff period in milliseconds (default 1000)
     */
    long delay() default 0;

    /**
     * The maximimum wait (in milliseconds) between retries. If less than the
     * {@link #delay()} then ignored.
     *
     * @return the maximum delay between retries (default 0 = ignored)
     */
    long maxDelay() default 0;

    /**
     * If positive, then used as a multiplier for generating the next delay for backoff.
     *
     * @return a multiplier to use to calculate the next backoff delay (default 0 =
     * ignored)
     */
    double multiplier() default 0;

    /**
     * In the exponential case ({@link #multiplier()}>0) set this to true to have the
     * backoff delays randomized, so that the maximum delay is multiplier times the
     * previous delay and the distribution is uniform between the two values.
     *
     * @return the flag to signal randomization is required (default false)
     */
    boolean random() default false;

}