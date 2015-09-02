package org.aggregateframework.session;

import org.aggregateframework.entity.AggregateRoot;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/**
 * User: changming.xie
 * Date: 14-8-19
 * Time: 下午4:30
 */
public class UnitOfWork extends AbstractClientSession {


    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T registerAggregate(AggregateEntry<T> aggregateEntry) {

        currentAggregateQueue.add(aggregateEntry);

        return aggregateEntry.getAggregateRoot();
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T registerToLocalCache(T entity) {
        localCacheIdentifiedEntityMap.put((Class<T>) entity.getClass(), entity.getId(), entity);
        return entity;
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T registerOriginalCopy(T entity) {
        T clonedEntity = SerializationUtils.clone(entity);
        originalCopyIdentifiedEntityMap.put((Class<T>) clonedEntity.getClass(), clonedEntity.getId(), clonedEntity);
        return entity;
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T findInLocalCache(Class<T> aggregateType, ID identifier) {
        T entity = localCacheIdentifiedEntityMap.get(aggregateType, identifier);
        return entity;
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T findOriginalCopy(Class<T> aggregateType, ID identifier) {
        T entity = originalCopyIdentifiedEntityMap.get(aggregateType, identifier);
        return entity;
    }

}
