package org.aggregateframework.cache;

import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class MultiLevelL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> implements L2Cache<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(MultiLevelL2Cache.class);

    private List<L2Cache<T, ID>> cacheProviders = new ArrayList<>();

    @Override
    public void remove(Collection<T> entities) {

        for (L2Cache<T, ID> l2Cache : cacheProviders) {
            try {
                l2Cache.remove(entities);
            } catch (Throwable e) {
                logger.error(String.format("remove cache failed. entity ids:%s", entities.stream().map(entity -> entity.getId().toString()).collect(Collectors.joining(","))), e);
            }
        }
    }

    @Override
    public void write(Collection<T> entities) {
        for (L2Cache<T, ID> l2Cache : cacheProviders) {
            try {
                l2Cache.write(entities);
            } catch (Throwable e) {
                logger.error(String.format("remove cache failed. entity ids:%s", entities.stream().map(entity -> entity.getId().toString()).collect(Collectors.joining(","))), e);
            }
        }
    }

    @Override
    public T findOne(Class<T> clazz, ID id) {
        for (L2Cache<T, ID> l2Cache : cacheProviders) {

            T entity = l2Cache.findOne(clazz, id);

            if (entity != null) {
                return entity;
            }
        }

        return null;
    }

    @Override
    public Collection<T> findAll(Class<T> aggregateType, Collection<ID> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<T>();
        }

        Map<ID, T> resultMap = new HashMap<ID, T>();

        Collection<ID> idsToFind = new ArrayList<>();

        for (L2Cache<T, ID> l2Cache : cacheProviders) {

            idsToFind.clear();
            idsToFind.addAll(ids.stream().filter(id -> !resultMap.containsKey(id)).collect(Collectors.toList()));

            if (CollectionUtils.isEmpty(idsToFind)) {
                break;
            }

            Collection<T> foundEntities = l2Cache.findAll(aggregateType, idsToFind);

            for (T entity : foundEntities) {
                resultMap.put(entity.getId(), entity);
            }
        }


        return resultMap.values();
    }

    public void setCachingProviders(List<L2Cache<T, ID>> cachingProviders) {
        this.cacheProviders = cachingProviders;
    }
}
