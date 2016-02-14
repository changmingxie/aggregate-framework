package org.aggregateframework.session;

import org.aggregateframework.eventhandling.processor.EventHandlerProcessor;
import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.eventhandling.EventInvokerEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author changming.xie
 */
public abstract class AbstractClientSession implements ClientSession {

    protected Queue<AggregateEntry> currentAggregateQueue = new ConcurrentLinkedQueue<AggregateEntry>();

    protected Queue<EventInvokerEntry> eventInvokerEntryQueue = new ConcurrentLinkedQueue<EventInvokerEntry>();

    protected final IdentifiedEntityMap localCacheIdentifiedEntityMap = new IdentifiedEntityMap();
    protected final IdentifiedEntityMap originalCopyIdentifiedEntityMap = new IdentifiedEntityMap();


    @Override
    public void flush() {
        doCommit();
    }

    @Override
    public void commit() {
        try {
            doCommit();
        } catch (RuntimeException e) {
            doRollback();
            throw e;
        } finally {
        }
    }

    @Override
    public void rollback() {
        doRollback();
    }

    public void addPostInvoker(EventInvokerEntry eventInvokerEntry) {
        eventInvokerEntryQueue.add(eventInvokerEntry);
    }

    @Override
    public void postHandle() {
        while (!eventInvokerEntryQueue.isEmpty()) {
            EventHandlerProcessor.proceed(eventInvokerEntryQueue.poll());
        }
    }

    protected void doCommit() {
        recursiveCommit();
        localCacheIdentifiedEntityMap.clear();
    }

    protected void recursiveCommit() {

        while (!currentAggregateQueue.isEmpty()) {

            Queue<AggregateEntry> thisAggregateQueue = currentAggregateQueue;

            AggregateEntry aggregateEntry = thisAggregateQueue.poll();

            aggregateEntry.saveAggregate();

            currentAggregateQueue = aggregateEntry.getChildren();

            List<EventMessage> messageList = new ArrayList<EventMessage>(aggregateEntry.getUncommittedDomainEvents());

            aggregateEntry.commitDomainEvents();

            EventMessage[] messages = messageList.toArray(new EventMessage[messageList.size()]);
            aggregateEntry.getEventBus().publish(messages);

            recursiveCommit();

            currentAggregateQueue = thisAggregateQueue;
        }
    }

    private void doRollback() {
        currentAggregateQueue.clear();
        eventInvokerEntryQueue.clear();
        localCacheIdentifiedEntityMap.clear();
    }

}
