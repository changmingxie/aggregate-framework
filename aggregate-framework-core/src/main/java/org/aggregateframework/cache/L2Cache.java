package org.aggregateframework.cache;

import org.aggregateframework.entity.AggregateRoot;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by changming.xie on 9/14/17.
 */
public interface L2Cache<T extends AggregateRoot<ID>, ID extends Serializable> {

    void remove(Collection<T> entities);

    void write(Collection<T> entities);

    T findOne(Class<T> clazz, ID id);

    Collection<T> findAll(Class<T> aggregateType, Collection<ID> ids);
}
