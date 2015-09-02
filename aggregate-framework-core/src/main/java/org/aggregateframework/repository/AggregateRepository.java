package org.aggregateframework.repository;

import org.aggregateframework.entity.AggregateRoot;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * User: changming.xie
 * Date: 14-6-25
 * Time: 下午1:31
 */
public interface AggregateRepository<T extends AggregateRoot<ID>, ID extends Serializable> extends CrudRepository<T,ID> {

    List<T> save(Collection<T> entities);

    List<T> findAll();

    List<T> findAll(Collection<ID> ids);

    public void flush();

}
