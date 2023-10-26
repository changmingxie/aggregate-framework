package org.aggregateframework.repository;

import org.aggregateframework.cache.L2Cache;
import org.aggregateframework.cache.NoL2Cache;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.eventbus.EventBus;
import org.aggregateframework.eventbus.SimpleEventBus;
import org.aggregateframework.exception.SystemException;
import org.aggregateframework.serializer.ObjectSerializer;
import org.aggregateframework.serializer.RegisterableKryoSerializer;
import org.aggregateframework.session.AggregateEntry;
import org.aggregateframework.session.SessionFactoryHelper;
import org.aggregateframework.utils.Assert;
import org.aggregateframework.utils.CollectionUtils;
import org.aggregateframework.utils.DomainObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.Serializable;
import java.net.SocketTimeoutException;
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
    protected SessionFactoryHelper sessionFactoryHelper = SessionFactoryHelper.INSTANCE;
    protected L2Cache<T, ID> l2Cache = NoL2Cache.INSTANCE;
    protected ObjectSerializer<T> objectSerializer = new RegisterableKryoSerializer<>();
    private EventBus eventBus = SimpleEventBus.INSTANCE;

    private SaveAggregateCallback<T> saveAggregateCallback = new SimpleSaveAggregateCallback();

    private List<Class<? extends Exception>> unknownStatusExceptions = Arrays.asList(SocketTimeoutException.class);

    protected AbstractAggregateRepository(Class<T> aggregateType) {
        this.aggregateType = aggregateType;
    }

    public void setUnknownStatusExceptions(List<Class<? extends Exception>> unknownStatusExceptions) {
        this.unknownStatusExceptions = unknownStatusExceptions;
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

                sessionFactoryHelper.requireClientSession().registerAggregate(aggregateEntry);

                for (T entity : entities) {
                    if (!entity.isNew()) {
                        sessionFactoryHelper.requireClientSession().registerToLocalCache(entity);
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
                sessionFactoryHelper.requireClientSession().flush();
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

                    T localCachedEntity = sessionFactoryHelper.requireClientSession().findInLocalCache(aggregateType, id);

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
                        sessionFactoryHelper.requireClientSession().registerToLocalCache(localEntity);
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
                sessionFactoryHelper.requireClientSession().registerOriginalCopy(entity);
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

    private <E> E execute(Callback<E> callback) {

        //try to register a new ClientSession, if not success, it means there exist a ClientSession with TransactionManager,
        //if success, then no existed ClientSession exists before, need manage the ClientSession lifecycle manually.
        boolean success = sessionFactoryHelper.hasActiveClientSession();

        if (success) {
            return callback.execute();
        } else {
            return executeWithNewClientSession(callback);
        }
    }

    private <E> E executeWithNewClientSession(Callback<E> callback) {

        sessionFactoryHelper.registerClientSessionIfAbsent();
        try {

            E result = null;
            try {
                result = callback.execute();
                sessionFactoryHelper.requireClientSession().commit();
            } catch (Exception e) {
                //the commit status maybe committed or rollback,
                //need check the exception e's type
                if (!isUnknownStatusException(e)) {
                    sessionFactoryHelper.requireClientSession().rollback();
                }

                throw e;
            }

            sessionFactoryHelper.requireClientSession().flushToL2Cache();
            sessionFactoryHelper.requireClientSession().postHandle();
            return result;

        } finally {
            sessionFactoryHelper.requireClientSession().clear();
            sessionFactoryHelper.closeClientSession();
        }
    }


    private boolean isUnknownStatusException(Throwable throwable) {

        Throwable rootCause = ExceptionUtils.getRootCause(throwable);

        if (unknownStatusExceptions != null) {

            for (Class unknownStatusException : unknownStatusExceptions) {

                if (unknownStatusException.isAssignableFrom(throwable.getClass())
                        || (rootCause != null && unknownStatusException.isAssignableFrom(rootCause.getClass()))) {
                    return true;
                }
            }
        }

        return false;
    }

    public interface Callback<E> {
        E execute();
    }

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

                sessionFactoryHelper.requireClientSession().attachL2Cache(aggregateType, l2Cache);
                sessionFactoryHelper.requireClientSession().removeFromL2Cache(needRemoveAggregates);

            }

            if (!CollectionUtils.isEmpty(needSaveAggregates)) {

                doSave(needSaveAggregates);

                sessionFactoryHelper.requireClientSession().attachL2Cache(aggregateType, l2Cache);
                sessionFactoryHelper.requireClientSession().writeToL2Cache(needSaveAggregates);
            }

            for (T entity : aggregateRoots) {
                sessionFactoryHelper.requireClientSession().registerToLocalCache(entity);
            }

            for (T entity : newAggregates) {
                T clonedEntity = objectSerializer.clone(entity);
                sessionFactoryHelper.requireClientSession().registerOriginalCopy(clonedEntity);
            }
        }

    }
}
