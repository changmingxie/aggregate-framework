package org.aggregateframework.eventhandling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: changming.xie
 * Date: 14-7-10
 * Time: 下午4:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

    boolean asynchronous() default false;

    AsyncConfig asyncConfig() default @AsyncConfig();

    boolean postAfterTransaction() default false;

    boolean isTransactionMessage() default false;

    int order() default Order.HIGHEST_PRECEDENCE;

    TransactionCheck transactionCheck() default @TransactionCheck();

    interface Order {
        /**
         * Useful constant for the highest precedence value.
         *
         * @see java.lang.Integer#MIN_VALUE
         */
        int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

        /**
         * Useful constant for the lowest precedence value.
         *
         * @see java.lang.Integer#MAX_VALUE
         */
        int LOWEST_PRECEDENCE = Integer.MAX_VALUE;
    }


}
