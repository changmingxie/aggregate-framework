package org.aggregateframework.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * User: changming.xie
 * Date: 14-6-23
 * Time: 下午2:46
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface DaoAwareQuery {

    public String mappedBy();

    public String select();

}
