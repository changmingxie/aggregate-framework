package org.aggregateframework.entity;

import org.aggregateframework.eventhandling.EventMessage;

import java.io.Serializable;
import java.util.Collection;

/**
 * User: changming.xie
 * Date: 14-6-25
 * Time: 下午1:34
 */
public interface AggregateRoot<ID extends Serializable> extends DomainObject<ID> {

    Collection<? extends EventMessage> getUncommittedDomainEvents();

    long getVersion();

    boolean isDeleted();

    void commitDomainEvents();

    void clearDomainEvents();
}
