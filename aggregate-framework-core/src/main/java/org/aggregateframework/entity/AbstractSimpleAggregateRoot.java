package org.aggregateframework.entity;

import org.aggregateframework.eventhandling.EventContainer;
import org.aggregateframework.eventhandling.EventMessage;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author changming.xie
 */
public abstract class AbstractSimpleAggregateRoot<ID extends Serializable> extends AbstractAggregateRoot<ID> {

    private ID id;

    private Long version = 1L;

    private Date createTime;

    private Date lastUpdateTime;

    @Transient
    private transient EventContainer domainEventContainer;

    @Transient
    private transient EventContainer applicationEventContainer;

    @Override
    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    @Override
    public EventContainer getDomainEventContainer() {
        ensureDomainEventContainerInitialized();
        return domainEventContainer;
    }

    @Override
    public EventContainer getApplicationEventContainer() {
        ensureApplicationEventContainerInitialized();
        return applicationEventContainer;
    }

    @Override
    public long getVersion() {
        return version;
    }



    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    private void ensureDomainEventContainerInitialized() {
        if (domainEventContainer == null) {
            domainEventContainer = new EventContainer();
        }
    }


    private void ensureApplicationEventContainerInitialized() {
        if (applicationEventContainer == null) {
            applicationEventContainer = new EventContainer();
        }
    }
}
