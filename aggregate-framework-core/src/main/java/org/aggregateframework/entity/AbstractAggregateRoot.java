package org.aggregateframework.entity;


import org.aggregateframework.eventhandling.EventContainer;
import org.aggregateframework.eventhandling.EventMessage;

import java.io.Serializable;
import java.util.Collection;

/**
 * User: changming.xie
 * Date: 14-6-26
 * Time: 下午1:54
 */
public abstract class AbstractAggregateRoot<ID extends Serializable> extends AbstractDomainObject<ID> implements AggregateRoot<ID> {


    private boolean isDeleted = false;

    protected void apply(Object eventPayload) {
        getDomainEventContainer().addEvent(eventPayload);
    }

    @Override
    public Collection<? extends EventMessage> getUncommittedDomainEvents() {
        return getDomainEventContainer().getEvents();
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void commitDomainEvents() {
        getDomainEventContainer().commit();
    }

    @Override
    public void clearDomainEvents() {
        getDomainEventContainer().clear();
    }

    public abstract EventContainer getDomainEventContainer();
}
