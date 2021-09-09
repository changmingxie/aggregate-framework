package org.aggregateframework.session;

import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.eventbus.EventBus;
import org.aggregateframework.repository.SaveAggregateCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by changmingxie on 6/18/15.
 */
public class AggregateEntry<T extends AggregateRoot> {

    private final Collection<T> aggregateRoots = new ArrayList<T>();
    private final SaveAggregateCallback<T> callback;

    private final EventBus eventBus;

    private final Queue<AggregateEntry> children = new ConcurrentLinkedQueue<AggregateEntry>();

    public AggregateEntry(Collection<T> aggregateRoots, SaveAggregateCallback<T> callback, EventBus eventBus) {
        this.aggregateRoots.addAll(aggregateRoots);
        this.callback = callback;
        this.eventBus = eventBus;
    }

    public void saveAggregate() {
        callback.save(aggregateRoots);
    }

    public Queue<AggregateEntry> getChildren() {
        return children;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public Collection<? extends EventMessage> getUncommittedDomainEvents() {

        List<EventMessage> uncommittedDomainEvents = new ArrayList<EventMessage>();

        for (T aggregateRoot : aggregateRoots) {
            uncommittedDomainEvents.addAll(aggregateRoot.getUncommittedDomainEvents());
        }
        return uncommittedDomainEvents;
    }

    public void commitDomainEvents() {

        for (T aggregateRoot : aggregateRoots) {
            aggregateRoot.commitDomainEvents();
        }
    }
}