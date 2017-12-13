package org.aggregateframework.session;

import org.aggregateframework.cache.L2Cache;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.eventhandling.EventInvokerEntry;

import java.io.Serializable;
import java.util.List;

/**
 * User: changming.xie
 * Date: 14-7-29
 * Time: 下午5:00
 */
public interface ClientSession {


    <T extends AggregateRoot<ID>, ID extends Serializable> void registerAggregate(AggregateEntry<T> aggregateEntry);

    void flush();

    void commit();

    void rollback();

    void postHandle();

    <T extends AggregateRoot<ID>, ID extends Serializable> T registerToLocalCache(T entity);

    <T extends AggregateRoot<ID>, ID extends Serializable> T removeFromLocalCache(T entity);

    <T extends AggregateRoot<ID>, ID extends Serializable> T registerOriginalCopy(T entity);

    public <T extends AggregateRoot<ID>, ID extends Serializable> T findInLocalCache(Class<T> aggregateType, ID identifier);

    public <T extends AggregateRoot<ID>, ID extends Serializable> T findOriginalCopy(Class<T> aggregateType, ID identifier);

    public <T extends AggregateRoot<ID>, ID extends Serializable> void removeFromL2Cache(List<T> entities);

    public <T extends AggregateRoot<ID>, ID extends Serializable> void writeToL2Cache(List<T> entities);

    public void flushToL2Cache();

    void addPostInvoker(EventInvokerEntry eventInvokerEntry);

    <T extends AggregateRoot<ID>, ID extends Serializable> void attachL2Cache(Class<T> aggregateType, L2Cache<T, ID> l2Cache);
}
