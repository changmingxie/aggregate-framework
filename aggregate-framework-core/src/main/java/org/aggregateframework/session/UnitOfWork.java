package org.aggregateframework.session;

import org.aggregateframework.entity.AggregateRoot;

import java.io.Serializable;

/**
 * User: changming.xie
 * Date: 14-8-19
 * Time: 下午4:30
 */
public class UnitOfWork extends AbstractClientSession {

    @Override
    public <T extends AggregateRoot<ID>, ID extends Serializable> void registerAggregate(AggregateEntry<T> aggregateEntry) {
        currentAggregateQueue.add(aggregateEntry);
    }
}
