package org.aggregateframework.context;

import org.aggregateframework.entity.DomainObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author changming.xie
 */
public class IdentifiedEntityMap {
    Map<Class<? extends DomainObject>, Map<Serializable, DomainObject>> fetchedEntityMap = new HashMap<Class<? extends DomainObject>, Map< Serializable, DomainObject>>();

    public <E extends DomainObject<ID>,ID extends Serializable> void put(Class<E> entityClass, ID id, E entity) {
        if (fetchedEntityMap.containsKey(entityClass)) {
            fetchedEntityMap.get(entityClass).put(id, entity);
        } else {
            fetchedEntityMap.put(entityClass, new HashMap<Serializable, DomainObject>());
            fetchedEntityMap.get(entityClass).put(id, entity);
        }
    }

    public <E extends DomainObject<ID>,ID extends Serializable> boolean containsKey(Class<E> oneToManyEntityClass, ID identifier) {

        if (!fetchedEntityMap.containsKey(oneToManyEntityClass)) {
            return false;
        }

        return fetchedEntityMap.get(oneToManyEntityClass).containsKey(identifier);

    }

    public <E extends DomainObject,ID extends Serializable>  E get(Class<E> oneToManyEntityClass, ID identifier) {
        if (!fetchedEntityMap.containsKey(oneToManyEntityClass)) {
            return null;
        }

        return (E)fetchedEntityMap.get(oneToManyEntityClass).get(identifier);
    }

    public void clear() {
        fetchedEntityMap.clear();
    }
}
