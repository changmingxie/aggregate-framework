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

    boolean postAfterTransaction() default false;

    String backOffMethod() default "";
}
