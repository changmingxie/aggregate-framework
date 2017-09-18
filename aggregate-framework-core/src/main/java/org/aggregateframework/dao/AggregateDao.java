package org.aggregateframework.dao;

import org.aggregateframework.entity.DomainObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by changming.xie on 10/28/16.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AggregateDao {
    Class<? extends DomainObject> value();
}
