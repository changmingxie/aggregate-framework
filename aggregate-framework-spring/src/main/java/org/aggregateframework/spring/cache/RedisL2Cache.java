package org.aggregateframework.spring.cache;

import org.aggregateframework.cache.L2Cache;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.retry.*;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by changming.xie on 9/17/17.
 */
public class RedisL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> implements L2Cache<T, ID> {

    RedisTemplate redisTemplate;

    private int maxAttempts = 3;

    private long expireTimeInSecond = 60*60*24;

    public RedisL2Cache() {
    }


    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public void setExpireTimeInSecond(long expireTimeInSecond) {
        this.expireTimeInSecond = expireTimeInSecond;
    }

    private String getKeyPrefix(Class aggregateType) {
        return aggregateType.getName() + ":";
    }

    @Override
    public void remove(final Collection<T> entities) {

        RetryTemplate retryTemplate = new RetryTemplate();

        RetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, new HashMap<Class<? extends Throwable>, Boolean>(), true);
        retryTemplate.setRetryPolicy(retryPolicy);

        RetryContext retryContext = retryPolicy.requireRetryContext();
        RetryCallback<Object> retryCallback = new RetryCallback<Object>() {
            @Override
            public Object doWithRetry(RetryContext context) {
                redisTemplate.executePipelined(new SessionCallback<Object>() {

                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {

                        for (T entity : entities) {
                            operations.delete(getKeyPrefix(entity.getClass()) + entity.getId());
                        }

                        return null;
                    }
                });
                return null;
            }
        };

        retryTemplate.execute(retryContext, retryCallback);
    }

    @Override
    public void write(final Collection<T> entities) {

        RetryTemplate retryTemplate = new RetryTemplate();

        RetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, new HashMap<Class<? extends Throwable>, Boolean>(), true);
        retryTemplate.setRetryPolicy(retryPolicy);

        RetryContext retryContext = retryPolicy.requireRetryContext();
        RetryCallback<Object> retryCallback = new RetryCallback<Object>() {
            @Override
            public Object doWithRetry(RetryContext context) {

                redisTemplate.executePipelined(new SessionCallback<Object>() {

                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {

                        for (T entity : entities) {
                            operations.opsForValue().set(getKeyPrefix(entity.getClass()) + entity.getId(), entity);
                            operations.expire(getKeyPrefix(entity.getClass()) + entity.getId(), expireTimeInSecond, TimeUnit.SECONDS);
                        }
                        return null;
                    }
                });

                return null;
            }
        };

        retryTemplate.execute(retryContext, retryCallback);
    }

    @Override
    public T findOne(Class<T> aggregateType, ID id) {

        Object object = redisTemplate.opsForValue().get(getKeyPrefix(aggregateType) + id);

        return (T) object;
    }

    @Override
    public Collection<T> findAll(final Class<T> aggregateType, final Collection<ID> ids) {

        List<T> result = redisTemplate.executePipelined(new SessionCallback<Object>() {

            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {

                for (ID id : ids) {
                    operations.opsForValue().get(getKeyPrefix(aggregateType) + id);
                }

                return null;
            }
        });

        List<T> fetchedEntities = new ArrayList<T>();

        for (T entity : result) {
            if (entity != null) {
                fetchedEntities.add(entity);
            }
        }

        return fetchedEntities;
    }
}
