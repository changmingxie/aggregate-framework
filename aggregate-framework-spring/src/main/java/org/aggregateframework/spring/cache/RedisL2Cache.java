package org.aggregateframework.spring.cache;

import org.aggregateframework.cache.L2Cache;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.retry.*;
import org.aggregateframework.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by changming.xie on 9/17/17.
 */
public class RedisL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> implements L2Cache<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(RedisL2Cache.class);

    private RedisTemplate redisTemplate;

    private int maxAttempts = 3;

    private long expireTimeInSecond = 60 * 60 * 24;

    private boolean suppressErrors = true;

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

        execute(new RetryCallback<Object>() {
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
        });
    }

    @Override
    public void write(final Collection<T> entities) {

        execute(new RetryCallback<Object>() {
            @Override
            public Object doWithRetry(RetryContext context) {

                redisTemplate.executePipelined(new SessionCallback<Object>() {

                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {

                        for (T entity : entities) {
                            String cacheKey = getKeyPrefix(entity.getClass()) + entity.getId();

                            operations.opsForHash().put(cacheKey, entity.getVersion(), entity);

                            if (entity.getVersion() > 1) {
                                // clean up entities with old versions
                                for (long i = 1L; i < entity.getVersion(); i++) {
                                    operations.opsForHash().delete(cacheKey, i);
                                }
                            }

                            operations.expire(cacheKey, expireTimeInSecond, TimeUnit.SECONDS);
                        }
                        return null;
                    }
                });

                return null;
            }
        });
    }

    @Override
    public T findOne(Class<T> aggregateType, ID id) {

        final String cacheKey = getKeyPrefix(aggregateType) + id;

        Map<Integer, T> entities = execute(new RetryCallback<Map<Integer, T>>() {
            @Override
            public Map<Integer, T> doWithRetry(RetryContext context) {
                return redisTemplate.opsForHash().entries(cacheKey);
            }
        });

        if (!CollectionUtils.isEmpty(entities)) {
            return entities.get(Collections.max(entities.keySet()));
        }

        return null;
    }

    @Override
    public Collection<T> findAll(final Class<T> aggregateType, final Collection<ID> ids) {

        List<Map<Integer, T>> result = execute(new RetryCallback<List<Map<Integer, T>>>() {
            @Override
            public List<Map<Integer, T>> doWithRetry(RetryContext context) {

                return redisTemplate.executePipelined(new SessionCallback<Object>() {

                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {

                        for (ID id : ids) {
                            operations.opsForHash().entries(getKeyPrefix(aggregateType) + id);
                        }

                        return null;
                    }
                });
            }
        });

        List<T> fetchedEntities = new ArrayList<T>();

        if (result != null) {
            for (Map<Integer, T> entities : result) {
                if (!CollectionUtils.isEmpty(entities)) {
                    T entity = entities.get(Collections.max(entities.keySet()));
                    fetchedEntities.add(entity);
                }
            }
        }

        return fetchedEntities;
    }

    private <T> T execute(RetryCallback<T> retryCallback) {

        RetryTemplate retryTemplate = new RetryTemplate();

        RetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, new HashMap<Class<? extends Throwable>, Boolean>(), true);
        retryTemplate.setRetryPolicy(retryPolicy);

        RetryContext retryContext = retryPolicy.requireRetryContext();

        return retryTemplate.execute(retryContext, retryCallback, new RecoveryCallback<T>() {
            @Override
            public T recover(RetryContext context) {
                if (suppressErrors) {
                    logger.warn("Redis operations failed but can be ignored");
                    return null;
                }

                throw new RuntimeException(context.getLastThrowable());
            }
        });
    }
}
