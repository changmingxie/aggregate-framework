package org.aggregateframework.session;

import org.aggregateframework.entity.AggregateRoot;

import java.io.Serializable;

/**
 * User: changming.xie
 * Date: 14-7-29
 * Time: 下午5:00
 */
public interface ClientSession {


    <T extends AggregateRoot<ID>,ID extends Serializable> T registerAggregate(AggregateEntry<T> aggregateEntry);

    void flush();

    void commit();

    void rollback();

    void postHandle();

    <T extends AggregateRoot<ID>,ID extends Serializable>  T registerToLocalCache(T entity);

    <T extends AggregateRoot<ID>,ID extends Serializable>  T registerOriginalCopy(T entity);

    public <T extends AggregateRoot<ID>,ID extends Serializable>  T findInLocalCache(Class<T> aggregateType, ID identifier);

    public <T extends AggregateRoot<ID>,ID extends Serializable>  T findOriginalCopy(Class<T> aggregateType, ID identifier);


}
