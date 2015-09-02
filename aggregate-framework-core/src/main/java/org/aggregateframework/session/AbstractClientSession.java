package org.aggregateframework.session;

import org.aggregateframework.context.IdentifiedEntityMap;
import org.aggregateframework.eventhandling.EventMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author changming.xie
 */
public abstract class AbstractClientSession implements ClientSession {

    protected Queue<AggregateEntry> currentAggregateQueue = new ConcurrentLinkedQueue<AggregateEntry>();

    protected Queue<AggregateEntry> applicationEventQueue = new ConcurrentLinkedQueue<AggregateEntry>();

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

    @Override
    public void postHandle() {

        while (!applicationEventQueue.isEmpty()) {

            AggregateEntry aggregateEntry = applicationEventQueue.poll();

            List<EventMessage> messageList = new ArrayList<EventMessage>(aggregateEntry.getAggregateRoot().getUncommittedApplicationEvents());

            aggregateEntry.getAggregateRoot().commitApplicationEvents();

            EventMessage[] messages = messageList.toArray(new EventMessage[messageList.size()]);
            aggregateEntry.getEventBus().publish(messages);
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

            List<EventMessage> messageList = new ArrayList<EventMessage>(aggregateEntry.getAggregateRoot().getUncommittedDomainEvents());

            aggregateEntry.getAggregateRoot().commitDomainEvents();

            EventMessage[] messages = messageList.toArray(new EventMessage[messageList.size()]);
            aggregateEntry.getEventBus().publish(messages);

            applicationEventQueue.add(aggregateEntry);

            recursiveCommit();

            currentAggregateQueue = thisAggregateQueue;
        }
    }

    private void doRollback() {
        currentAggregateQueue.clear();
        applicationEventQueue.clear();
        localCacheIdentifiedEntityMap.clear();
    }

}
