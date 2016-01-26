package org.aggregateframework.entity;

import org.aggregateframework.eventhandling.EventContainer;

import java.io.Serializable;
import java.util.Date;

/**
 * @author changming.xie
 */
public abstract class AbstractSimpleAggregateRoot<ID extends Serializable> extends AbstractAggregateRoot<ID> {

    private static final long serialVersionUID = 5687124586118075949L;

    private ID id;

    private Long version = 1L;

    private Date createTime;

    private Date lastUpdateTime;

    @Transient
    private transient EventContainer domainEventContainer;

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
}
