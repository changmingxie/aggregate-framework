package org.aggregateframework.repository;

import org.aggregateframework.SystemException;
import org.aggregateframework.cache.L2Cache;
import org.aggregateframework.cache.NoL2Cache;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.eventbus.EventBus;
import org.aggregateframework.eventbus.SimpleEventBus;
import org.aggregateframework.serializer.KryoPoolSerializer;
import org.aggregateframework.serializer.ObjectSerializer;
import org.aggregateframework.session.AggregateEntry;
import org.aggregateframework.session.LocalSessionFactory;
import org.aggregateframework.session.SessionFactory;
import org.aggregateframework.utils.Assert;
import org.aggregateframework.utils.CollectionUtils;
import org.aggregateframework.utils.DomainObjectUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User: changming.xie
 * Date: 14-7-21
 * Time: 下午6:51
 */
public abstract class AbstractAggregateRepository<T extends AggregateRoot<ID>, ID extends Serializable> implements AggregateRepository<T, ID> {

    protected final Class<T> aggregateType;
    protected SessionFactory sessionFactory = LocalSessionFactory.INSTANCE;
    private EventBus eventBus = SimpleEventBus.INSTANCE;
    private SaveAggregateCallback<T> saveAggregateCallback = new SimpleSaveAggregateCallback();

    protected L2Cache<T, ID> l2Cache = NoL2Cache.INSTANCE;

    protected ObjectSerializer<T> objectSerializer = new KryoPoolSerializer<T>();

    protected AbstractAggregateRepository(Class<T> aggregateType) {
        this.aggregateType = aggregateType;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setL2Cache(L2Cache<T, ID> l2Cache) {
        this.l2Cache = l2Cache;
    }

    public void setObjectSerializer(ObjectSerializer<T> objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    @Override
    public T save(T entity) {
        save(Arrays.asList(entity));
        return entity;
    }

    @Override
    public List<T> save(final Collection<T> entities) {

        return execute(new Callback<List<T>>() {
            @Override
            public List<T> execute() {
                List<T> result = new ArrayList<T>();

                if (CollectionUtils.isEmpty(entities)) {
                    return result;
                }

                AggregateEntry<T> aggregateEntry = new AggregateEntry<T>(entities, saveAggregateCallback, eventBus);

                sessionFactory.requireClientSession().registerAggregate(aggregateEntry);

                for (T entity : entities) {
                    if (!entity.isNew()) {
                        sessionFactory.requireClientSession().registerToLocalCache(entity);
                    }
                }

                result.addAll(entities);
                return result;
            }
        });
    }

    @Override
    public void flush() {
        execute(new Callback<Boolean>() {
            @Override
            public Boolean execute() {
                sessionFactory.requireClientSession().flush();
                return Boolean.TRUE;
            }
        });
    }

    @Override
    public T findOne(final ID id) {

        List<T> fetchedEntities = findAll(Arrays.asList(id));

        if (fetchedEntities.size() > 0) {
            return fetchedEntities.get(0);
        }

        return null;
    }

    @Override
    public boolean exists(ID id) {

        T entity = findOne(id);

        return entity != null;
    }

    @Override
    public List<T> findAll() {
        List<ID> ids = doFindAllIds();
        return findAll(ids);
    }

    @Override
    public List<T> findAll(final Collection<ID> ids) {

        return execute(new Callback<List<T>>() {
            @Override
            public List<T> execute() {
                List<T> entities = new ArrayList<T>();

                if (CollectionUtils.isEmpty(ids)) {
                    return entities;
                }

                List<ID> idsNeedFetch = new ArrayList<ID>(ids);

                List<ID> idsInLocalCache = new ArrayList<ID>();

                for (ID id : idsNeedFetch) {

                    T localCachedEntity = sessionFactory.requireClientSession().findInLocalCache(aggregateType, id);

                    if (localCachedEntity != null) {

                        entities.add(localCachedEntity);
                        idsInLocalCache.add(id);
                    }
                }

                idsNeedFetch.removeAll(idsInLocalCache);

                if (!CollectionUtils.isEmpty(idsNeedFetch)) {

                    Collection<T> fetchedEntitiesFromStore = findFromStore(idsNeedFetch);

                    for (T entity : fetchedEntitiesFromStore) {
                        T localEntity = objectSerializer.clone(entity);
                        sessionFactory.requireClientSession().registerToLocalCache(localEntity);
                        entities.add(localEntity);
                    }
                }

                return entities;
            }
        });
    }


    @Override
    public long count() {
        return doCount();
    }

    @Override
    public void delete(ID id) {
        Assert.notNull(id, "The given id must not be null!");

        if (!exists(id)) {
            throw new SystemException("entity does not exists");
        }

        delete(findOne((ID) id));
    }

    @Override
    public void delete(T entity) {
        DomainObjectUtils.setField(entity, DomainObjectUtils.IS_DELETED, true);

        save(entity);
    }

    @Override
    public void delete(Collection<T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");

        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        for (T element : findAll()) {
            delete(element);
        }
    }

    protected Collection<T> findFromStore(Collection<ID> ids) {

        Collection<ID> needFetchedIds = new ArrayList<ID>(ids);

        Collection<T> fetchedEntities = new ArrayList<T>();

        if (!CollectionUtils.isEmpty(needFetchedIds)) {

            Collection<T> fetchedEntitiesFromL2Cache = l2Cache.findAll(aggregateType, needFetchedIds);

            List<ID> idsFetchedFromL2Cache = new ArrayList<ID>();

            for (T entity : fetchedEntitiesFromL2Cache) {
                idsFetchedFromL2Cache.add(entity.getId());
            }

            needFetchedIds.removeAll(idsFetchedFromL2Cache);

            Collection<T> fetchedEntitiesFromStore = doFindAll(needFetchedIds);
            l2Cache.write(fetchedEntitiesFromStore);


            fetchedEntities.addAll(fetchedEntitiesFromL2Cache);
            fetchedEntities.addAll(fetchedEntitiesFromStore);

            for (T entity : fetchedEntities) {
                entity.clearDomainEvents();
                sessionFactory.requireClientSession().registerOriginalCopy(entity);
            }
        }

        return fetchedEntities;
    }

    protected abstract Collection<T> doSave(Collection<T> entities);

    protected abstract void doRemove(Collection<T> aggregates);

    protected abstract T doFindOne(ID id);

    protected abstract boolean doExists(ID id);

    protected abstract List<ID> doFindAllIds();

    protected abstract List<T> doFindAll(Collection<ID> ids);

    protected abstract long doCount();

    private class SimpleSaveAggregateCallback implements SaveAggregateCallback<T> {
        @Override
        public void save(final Collection<T> aggregateRoots) {

            List<T> needRemoveAggregates = new ArrayList<T>();

            List<T> needSaveAggregates = new ArrayList<T>();

            List<T> newAggregates = new ArrayList<T>();

            for (T aggregate : aggregateRoots) {
                if (aggregate.isDeleted()) {
                    needRemoveAggregates.add(aggregate);
                } else {
                    needSaveAggregates.add(aggregate);

                    if (aggregate.isNew()) {
                        newAggregates.add(aggregate);
                    }
                }
            }

            if (!CollectionUtils.isEmpty(needRemoveAggregates)) {

                doRemove(needRemoveAggregates);

                sessionFactory.requireClientSession().attachL2Cache(aggregateType, l2Cache);
                sessionFactory.requireClientSession().removeFromL2Cache(needRemoveAggregates);

            }

            if (!CollectionUtils.isEmpty(needSaveAggregates)) {

                doSave(needSaveAggregates);

                sessionFactory.requireClientSession().attachL2Cache(aggregateType, l2Cache);
                sessionFactory.requireClientSession().writeToL2Cache(needSaveAggregates);
            }

            for (T entity : aggregateRoots) {
                sessionFactory.requireClientSession().registerToLocalCache(entity);
            }

            for (T entity : newAggregates) {
                T clonedEntity = objectSerializer.clone(entity);
                sessionFactory.requireClientSession().registerOriginalCopy(clonedEntity);
            }
        }

    }

    private <E> E execute(Callback<E> callback) {

        boolean success = sessionFactory.registerClientSession(false);

        try {

            E result = callback.execute();

            if (success) {
                sessionFactory.requireClientSession().commit();
                sessionFactory.requireClientSession().flushToL2Cache();
                sessionFactory.requireClientSession().postHandle();
            }

            return result;

        } finally {
            if (success) {
                sessionFactory.closeClientSession();
            }
        }
    }

    public interface Callback<E> {
        E execute();
    }
}
