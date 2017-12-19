package org.aggregateframework.eventhandling.annotation;

/**
 * Created by changming.xie on 12/19/17.
 */
public @interface TransactionCheck {

    String checkTransactionStatusMethod() default "";

    String compensableTransactionRepository() default "transactionRepository";
}
