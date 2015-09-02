package org.aggregateframework.session;

import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.eventhandling.EventBus;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by changmingxie on 6/18/15.
 */
public class AggregateEntry<T extends AggregateRoot> {

    private final T aggregateRoot;
    private final SaveAggregateCallback<T> callback;

    private final EventBus eventBus;

    private final Queue<AggregateEntry> children = new ConcurrentLinkedQueue<AggregateEntry>();

    public AggregateEntry(T aggregateRoot, SaveAggregateCallback<T> callback, EventBus eventBus) {
        this.aggregateRoot = aggregateRoot;
        this.callback = callback;
        this.eventBus = eventBus;
    }

    public T getAggregateRoot() {
        return aggregateRoot;
    }

    public void saveAggregate() {
        callback.save(aggregateRoot);
    }

    public Queue<AggregateEntry> getChildren() {
        return children;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}