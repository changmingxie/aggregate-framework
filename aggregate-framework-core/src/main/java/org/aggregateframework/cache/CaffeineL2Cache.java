package org.aggregateframework.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.aggregateframework.entity.AggregateRoot;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CaffeineL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> implements L2Cache<T, ID> {


    private long expireTimeInSecond = 2; //2 second

    private int maximumSize = 10000;

    private volatile Cache<String, T> cache = null;

    public CaffeineL2Cache() {

    }

    public void setExpireTimeInSecond(long expireTimeInSecond) {
        this.expireTimeInSecond = expireTimeInSecond;
    }

    public void setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
    }

    @Override
    public void remove(Collection<T> entities) {
        getCache().invalidateAll(entities.stream().map(entity -> getCacheKey(entity.getClass(), entity.getId())).collect(Collectors.toList()));
    }

    @Override
    public void write(Collection<T> entities) {
        getCache().putAll(entities.stream().collect(Collectors.toMap(entity -> getCacheKey(entity.getClass(), entity.getId()), entity -> entity)));
    }

    @Override
    public T findOne(Class<T> clazz, ID id) {
        return getCache().getIfPresent(getCacheKey(clazz, id));
    }

    @Override
    public Collection<T> findAll(Class<T> aggregateType, Collection<ID> ids) {
        return getCache().getAllPresent(ids.stream().map(id -> getCacheKey(aggregateType, id)).collect(Collectors.toList())).values();
    }

    private String getCacheKey(Class clazz, ID id) {
        return String.format("%s:%s", clazz.getCanonicalName(), id);
    }

    private Cache<String, T> getCache() {

        if (cache == null) {
            synchronized (this) {
                if (cache == null) {
                    cache = Caffeine.newBuilder()
                            .expireAfterWrite(expireTimeInSecond, TimeUnit.SECONDS)
                            .maximumSize(maximumSize)
                            .build();
                }
            }
        }

        return cache;
    }
}
