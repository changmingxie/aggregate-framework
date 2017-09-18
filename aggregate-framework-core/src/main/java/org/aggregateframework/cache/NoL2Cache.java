package org.aggregateframework.cache;

import org.aggregateframework.entity.AggregateRoot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by changming.xie on 9/17/17.
 */
public class NoL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> implements L2Cache<T, ID> {

    public static NoL2Cache INSTANCE = new NoL2Cache();

    @Override
    public void remove(Collection<T> entities) {

    }

    @Override
    public void write(Collection<T> entities) {

    }

    @Override
    public T findOne(Class<T> clazz, ID id) {
        return null;
    }

    @Override
    public Collection<T> findAll(Class<T> aggregateType, Collection<ID> ids) {
        return new ArrayList<T>();
    }
}
