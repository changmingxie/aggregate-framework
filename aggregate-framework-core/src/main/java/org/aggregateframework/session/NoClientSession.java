package org.aggregateframework.session;

import org.aggregateframework.entity.AggregateRoot;

import java.io.Serializable;

/**
 * Created by changmingxie on 6/19/15.
 */
public class NoClientSession extends AbstractClientSession {
    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T registerAggregate(AggregateEntry<T> aggregateEntry) {
        currentAggregateQueue.add(aggregateEntry);
        flush();
        postHandle();
        return aggregateEntry.getAggregateRoot();
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T registerToLocalCache(T entity) {
        return entity;
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T registerOriginalCopy(T entity) {
        return entity;
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T findInLocalCache(Class<T> aggregateType, ID identifier) {
        return null;
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T findOriginalCopy(Class<T> aggregateType, ID identifier) {
        return null;
    }
}
