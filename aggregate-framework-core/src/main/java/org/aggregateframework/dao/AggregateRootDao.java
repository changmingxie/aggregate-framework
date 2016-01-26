package org.aggregateframework.dao;

import org.aggregateframework.entity.AggregateRoot;

import java.io.Serializable;
import java.util.List;

/**
 * User: changming.xie
 * Date: 14-6-26
 * Time: 上午10:14
 */
public interface AggregateRootDao<T extends AggregateRoot<ID>, ID extends Serializable> extends DomainObjectDao<T, ID> {

    List<T> findAll();

    long count();
}
