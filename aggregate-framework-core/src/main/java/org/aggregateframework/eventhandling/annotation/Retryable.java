package org.aggregateframework.eventhandling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by changming.xie on 2/1/16.
 * part of the source code come from open source:spring-retry.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {

    /**
     * Exception types that are retryable. Synonym for includes(). Defaults to
     * empty (and if excludes is also empty all exceptions are retried).
     *
     * @return exception types to retry
     */
    Class<? extends Throwable>[] value() default {};

    /**
     * Exception types that are retryable. Defaults to empty (and if excludes is
     * also empty all exceptions are retried).
     *
     * @return exception types to retry
     */
    Class<? extends Throwable>[] include() default {};

    /**
     * Exception types that are not retryable. Defaults to empty (and if
     * includes is also empty all exceptions are retried).
     *
     * @return exception types to retry
     */
    Class<? extends Throwable>[] exclude() default {};

    /**
     * @return the maximum number of attempts (including the first failure),
     * defaults to 3
     */
    int maxAttempts() default 3;

    /**
     * Specify the backoff properties for retrying this operation. The default is
     * no backoff, but it can be a good idea to pause between attempts (even at
     * the cost of blocking a thread).
     *
     * @return a backoff specification
     */
    Backoff backoff() default @Backoff();

    /*
    * Specify the recover method to call when get max attempts.
    */
    String recoverMethod() default "";

}
