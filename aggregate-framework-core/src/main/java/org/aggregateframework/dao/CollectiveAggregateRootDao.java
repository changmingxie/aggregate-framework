package org.aggregateframework.dao;

import org.aggregateframework.entity.AggregateRoot;

import java.io.Serializable;

/**
 * Created by changmingxie on 1/21/16.
 */
public interface CollectiveAggregateRootDao<T extends AggregateRoot<ID>, ID extends Serializable> extends CollectiveDomainObjectDao<T, ID>, AggregateRootDao<T, ID> {

}
