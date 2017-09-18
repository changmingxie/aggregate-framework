package org.aggregateframework.cache;

import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.entity.DomainObject;

import java.io.Serializable;
import java.util.*;

/**
 * @author changming.xie
 */
public class IdentifiedEntityMap {

    Map<Class<? extends DomainObject>, Map<Serializable, DomainObject>> fetchedEntityMap = new LinkedHashMap<Class<? extends DomainObject>, Map<Serializable, DomainObject>>();


    public <E extends DomainObject<ID>, ID extends Serializable> void put(Class<E> entityClass, Collection<E> entities) {
        for (E entity : entities) {
            put(entityClass, entity);
        }
    }

    public <E extends DomainObject<ID>, ID extends Serializable> void put(Class<E> entityClass, E entity) {
        put(entityClass, entity.getId(), entity);
    }


    public <E extends DomainObject<ID>, ID extends Serializable> void put(Class<E> entityClass, ID id, E entity) {
        if (fetchedEntityMap.containsKey(entityClass)) {
            fetchedEntityMap.get(entityClass).put(id, entity);
        } else {
            fetchedEntityMap.put(entityClass, new LinkedHashMap<Serializable, DomainObject>());
            fetchedEntityMap.get(entityClass).put(id, entity);
        }
    }

    public <T extends AggregateRoot<ID>, ID extends Serializable> void remove(Class<T> entityClass, ID id) {
        if (fetchedEntityMap.containsKey(entityClass)) {
            fetchedEntityMap.get(entityClass).remove(id);
        }
    }

    public <E extends DomainObject<ID>, ID extends Serializable> boolean containsKey(Class<E> oneToManyEntityClass, ID identifier) {

        if (!fetchedEntityMap.containsKey(oneToManyEntityClass)) {
            return false;
        }

        return fetchedEntityMap.get(oneToManyEntityClass).containsKey(identifier);

    }

    public <E extends DomainObject, ID extends Serializable> E get(Class<E> oneToManyEntityClass, ID identifier) {
        if (!fetchedEntityMap.containsKey(oneToManyEntityClass)) {
            return null;
        }

        return (E) fetchedEntityMap.get(oneToManyEntityClass).get(identifier);
    }

    public void clear() {
        fetchedEntityMap.clear();
    }

    public List<DomainObject> getAllEntities() {

        List<DomainObject> entities = new ArrayList<DomainObject>();

        for (Map<Serializable, DomainObject> value : fetchedEntityMap.values()) {

            entities.addAll(value.values());
        }

        return entities;
    }

}
