package org.aggregateframework.dao;

import org.aggregateframework.entity.DomainObject;

import java.io.Serializable;

/**
 * @author changming.xie
 */
public interface DomainObjectDao<T extends DomainObject<ID>, ID extends Serializable> {

    int insert(T entity);

    int delete(T entity);

    int update(T entity);

    T findById(ID id);
}
