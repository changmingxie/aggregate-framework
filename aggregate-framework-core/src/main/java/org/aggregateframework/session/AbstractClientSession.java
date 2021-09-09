package org.aggregateframework.session;

import org.aggregateframework.cache.IdentifiedEntityMap;
import org.aggregateframework.cache.L2Cache;
import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.entity.DomainObject;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.processor.EventHandlerProcessor;
import org.aggregateframework.transaction.LocalTransactionExecutor;
import org.aggregateframework.transaction.LocalTransactionState;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author changming.xie
 */
public abstract class AbstractClientSession implements ClientSession {

    protected Queue<AggregateEntry> currentAggregateQueue = new ConcurrentLinkedQueue<AggregateEntry>();
    protected Queue<EventInvokerEntry> postEventInvokerEntryQueue = new ConcurrentLinkedQueue<EventInvokerEntry>();
    protected Set<EventInvokerEntry> transactionalEventInvokerEntrySet = new ConcurrentSkipListSet<EventInvokerEntry>();
//    protected Set<AggregateEntry> allAggregateEntrySet = new ConcurrentSkipListSet<AggregateEntry>();

    protected final IdentifiedEntityMap localCacheIdentifiedEntityMap = new IdentifiedEntityMap();
    protected final IdentifiedEntityMap originalCopyIdentifiedEntityMap = new IdentifiedEntityMap();
    protected final IdentifiedEntityMap removeFromL2CacheIdentifiedEntityMap = new IdentifiedEntityMap();
    protected final IdentifiedEntityMap writeToL2CacheIdentifiedEntityMap = new IdentifiedEntityMap();

    private final Map<Class<? extends AggregateRoot>, L2Cache> l2CacheMap = new HashMap<Class<? extends AggregateRoot>, L2Cache>();

    @Override
    public void flush() {
        doCommit();
    }

    @Override
    public void commit() {
        try {
            doCommit();
        } catch (Throwable e) {
            doRollback();
            throw e;
        } finally {
        }
    }

    @Override
    public void rollback() {
        doRollback();
    }


    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T registerToLocalCache(T entity) {
        localCacheIdentifiedEntityMap.put((Class<T>) entity.getClass(), entity.getId(), entity);
        return entity;
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T removeFromLocalCache(T entity) {
        localCacheIdentifiedEntityMap.remove(entity.getClass(), entity.getId());
        return null;
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> T registerOriginalCopy(T entity) {
        originalCopyIdentifiedEntityMap.put((Class<T>) entity.getClass(), entity.getId(), entity);
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


    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> void attachL2Cache(Class<T> aggregateType, L2Cache<T, ID> l2Cache) {
        l2CacheMap.put(aggregateType, l2Cache);
    }

    public void addPostInvoker(EventInvokerEntry eventInvokerEntry) {
        postEventInvokerEntryQueue.add(eventInvokerEntry);
    }

    public void addTransactionalInvoker(EventInvokerEntry eventInvokerEntry) {
        transactionalEventInvokerEntrySet.add(eventInvokerEntry);
    }

    @Override
    public void postHandle() {
        try {

            SessionFactoryHelper.INSTANCE.startNewSessionFactory();

            while (!postEventInvokerEntryQueue.isEmpty()) {
                EventHandlerProcessor.proceed(postEventInvokerEntryQueue.poll());
            }
        } finally {
            SessionFactoryHelper.INSTANCE.closeSessionFactory();
        }
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> void removeFromL2Cache(List<T> entities) {
        for (T entity : entities) {
            removeFromL2CacheIdentifiedEntityMap.put((Class<T>) entity.getClass(), entity.getId(), entity);
        }
    }

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> void writeToL2Cache(List<T> entities) {
        for (T entity : entities) {
            writeToL2CacheIdentifiedEntityMap.put((Class<T>) entity.getClass(), entity.getId(), entity);
        }
    }

    @Override
    public void flushToL2Cache() {

        Map<Class<? extends DomainObject>, Map<Serializable, DomainObject>> removedAggregateTypeEntityMap = removeFromL2CacheIdentifiedEntityMap.getAggregateTypeEntityMap();

        for (Map.Entry<Class<? extends DomainObject>, Map<Serializable, DomainObject>> entry : removedAggregateTypeEntityMap.entrySet()) {

            L2Cache l2Cache = l2CacheMap.get(entry.getKey());
            if (l2Cache != null) {
                l2Cache.remove(entry.getValue().values());
            }
        }

        Map<Class<? extends DomainObject>, Map<Serializable, DomainObject>> updatedAggregateTypeEntityMap = writeToL2CacheIdentifiedEntityMap.getAggregateTypeEntityMap();

        for (Map.Entry<Class<? extends DomainObject>, Map<Serializable, DomainObject>> entry : updatedAggregateTypeEntityMap.entrySet()) {

            L2Cache l2Cache = l2CacheMap.get(entry.getKey());
            if (l2Cache != null) {
                l2Cache.write(entry.getValue().values());
            }
        }

        removeFromL2CacheIdentifiedEntityMap.clear();
        writeToL2CacheIdentifiedEntityMap.clear();
    }

    protected void doCommit() {
        recursiveCommit();
    }

    protected void recursiveCommit() {

        while (!currentAggregateQueue.isEmpty()) {

            Queue<AggregateEntry> thisAggregateQueue = currentAggregateQueue;

            final AggregateEntry aggregateEntry = thisAggregateQueue.poll();

            List<EventMessage> messageList = new ArrayList<EventMessage>(aggregateEntry.getUncommittedDomainEvents());

            aggregateEntry.getEventBus().publishInTransaction(messageList, new LocalTransactionExecutor() {

                @Override
                public LocalTransactionState executeLocalTransactionBranch(List<EventMessage> events) {

                    aggregateEntry.saveAggregate();

                    aggregateEntry.commitDomainEvents();

                    return LocalTransactionState.COMMIT_MESSAGE;
                }
            });

            currentAggregateQueue = aggregateEntry.getChildren();

            recursiveCommit();

            currentAggregateQueue = thisAggregateQueue;
        }
    }

    private void doRollback() {

        try {
            for (EventInvokerEntry eventInvokerEntry : transactionalEventInvokerEntrySet) {
                EventHandlerProcessor.cancel(eventInvokerEntry);
            }
        } finally {
            currentAggregateQueue.clear();
            postEventInvokerEntryQueue.clear();
            transactionalEventInvokerEntrySet.clear();

            localCacheIdentifiedEntityMap.clear();
            originalCopyIdentifiedEntityMap.clear();
            removeFromL2CacheIdentifiedEntityMap.clear();
            writeToL2CacheIdentifiedEntityMap.clear();
        }
    }

}
