package org.aggregateframework.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * User: changming.xie
 * Date: 2014-09-29
 * Time: 15:49
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Transient {
}
