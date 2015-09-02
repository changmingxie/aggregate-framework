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

    protected void applyDomainEvent(Object eventPayload) {
        getDomainEventContainer().addEvent(eventPayload);
    }

    protected void applyApplicationEvent(Object eventPayload) {
        getApplicationEventContainer().addEvent(eventPayload);
    }

    @Override
    public Collection<? extends EventMessage> getUncommittedDomainEvents() {
        return getDomainEventContainer().getEvents();
    }

    @Override
    public Collection<? extends EventMessage> getUncommittedApplicationEvents() {
        return getApplicationEventContainer().getEvents();
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

    @Override
    public void commitApplicationEvents() {
        getApplicationEventContainer().commit();
    }

    @Override
    public void clearApplicationEvents() {
        getApplicationEventContainer().clear();
    }

    public abstract EventContainer getDomainEventContainer();

    public abstract EventContainer getApplicationEventContainer();
}
