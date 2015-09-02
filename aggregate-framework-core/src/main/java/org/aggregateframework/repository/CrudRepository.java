package org.aggregateframework.repository;

import java.io.Serializable;
import java.util.Collection;

/**
 * User: changming.xie
 * Date: 14-6-25
 * Time: 上午10:14
 */
public interface CrudRepository<T,ID extends Serializable> extends Repository<T,ID> {


    T save(T entity);

    Collection<T> save(Collection<T> entities);

    T findOne(ID id);

    boolean exists(ID id);

    Collection<T> findAll();

    Collection<T> findAll(Collection<ID> ids);

    long count();

    void delete(ID id);

    void delete(T entity);

    void delete(Collection<T> entities);

    void deleteAll();

}
