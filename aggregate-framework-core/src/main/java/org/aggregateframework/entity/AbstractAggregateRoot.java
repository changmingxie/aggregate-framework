package org.aggregateframework.entity;


import org.aggregateframework.domainevent.EventMessage;

import java.io.Serializable;
import java.util.Collection;

/**
 * User: changming.xie
 * Date: 14-6-26
 * Time: 下午1:54
 */
public abstract class AbstractAggregateRoot<ID extends Serializable> extends AbstractDomainObject<ID> implements AggregateRoot<ID> {

    private static final long serialVersionUID = -7295689940261849143L;

    private transient boolean isDeleted = false;

    @Transient
    private transient EventContainer domainEventContainer;

    protected void apply(Object eventPayload) {
        getDomainEventContainer().addEvent(eventPayload);
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public Collection<? extends EventMessage> getUncommittedDomainEvents() {
        return getDomainEventContainer().getEvents();
    }

    @Override
    public void commitDomainEvents() {
        getDomainEventContainer().commit();
    }

    @Override
    public void clearDomainEvents() {
        getDomainEventContainer().clear();
    }

    public EventContainer getDomainEventContainer() {
        ensureDomainEventContainerInitialized();
        return domainEventContainer;
    }

    private void ensureDomainEventContainerInitialized() {
        if (domainEventContainer == null) {
            domainEventContainer = new EventContainer();
        }
    }
}
