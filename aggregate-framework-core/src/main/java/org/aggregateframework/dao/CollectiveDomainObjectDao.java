package org.aggregateframework.dao;

import org.aggregateframework.entity.DomainObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by changmingxie on 1/21/16.
 */
public interface CollectiveDomainObjectDao<T extends DomainObject<ID>, ID extends Serializable> extends DomainObjectDao<T, ID> {

    int insertAll(Collection<T> entities);

    int deleteAll(Collection<T> entities);

    int updateAll(Collection<T> entities);

    List<T> findByIds(Collection<ID> ids);
}
