package org.aggregateframework.cache;

import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.retry.*;
import org.aggregateframework.serializer.KryoPoolSerializer;
import org.aggregateframework.serializer.ObjectSerializer;
import org.aggregateframework.transaction.repository.helper.CommandCallback;
import org.aggregateframework.transaction.repository.helper.RedisCommands;
import org.aggregateframework.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Deprecated
public abstract class AbstractRedisHashL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> implements L2Cache<T, ID> {

    private static final String KEY_PREFIX_CONSTANT = "AGG:L2CACHE:";

    private static final Logger logger = LoggerFactory.getLogger(ShardJedisL2Cache.class);

    private ObjectSerializer<T> valueSerializer = new KryoPoolSerializer();

    private int maxAttempts = 2;

    private int expireTimeInSecond = 60 * 30; //30 minute

    private boolean suppressErrors = true;

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public void setExpireTimeInSecond(int expireTimeInSecond) {
        this.expireTimeInSecond = expireTimeInSecond;
    }

    public ObjectSerializer<T> getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(ObjectSerializer<T> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    private String getKeyPrefix(Class aggregateType) {
        return KEY_PREFIX_CONSTANT + aggregateType.getName() + ":";
    }

    public abstract RedisCommands getRedisCommands();

    @Override
    public void remove(final Collection<T> entities) {

        execute(new RetryCallback<Object>() {
            @Override
            public Object doWithRetry(RetryContext context) {

                try (RedisCommands redisCommands = getRedisCommands()) {

                    redisCommands.executePipelined(new CommandCallback<List<Object>>() {
                        @Override
                        public List<Object> execute(RedisCommands commands) {

                            for (T entity : entities) {
                                commands.del((getKeyPrefix(entity.getClass()) + entity.getId()).getBytes());
                            }
                            return null;
                        }
                    });
                } catch (IOException e) {
                    logger.error("remove cache failed.", e);
                }
                return null;
            }
        });
    }

    @Override
    public void write(final Collection<T> entities) {

        execute(new RetryCallback<Object>() {
            @Override
            public Object doWithRetry(RetryContext context) {

                try (RedisCommands redisCommands = getRedisCommands()) {

                    redisCommands.executePipelined(new CommandCallback<List<Object>>() {
                        @Override
                        public List<Object> execute(RedisCommands commands) {

                            for (T entity : entities) {
                                String cacheKey = getKeyPrefix(entity.getClass()) + entity.getId();
                                commands.hset(cacheKey.getBytes(), String.valueOf(entity.getVersion()).getBytes(), valueSerializer.serialize(entity));

                                if (entity.getVersion() > 1) {
                                    // clean up entities with old versions
                                    for (long i = 1L; i < entity.getVersion(); i++) {
                                        commands.hdel(cacheKey.getBytes(), String.valueOf(i).getBytes());
                                    }
                                }

                                commands.expire(cacheKey.getBytes(), expireTimeInSecond);
                            }

                            return null;
                        }
                    });
                } catch (IOException e) {
                    logger.error("write cache failed.", e);
                }

                return null;
            }
        });
    }

    @Override
    public T findOne(Class<T> aggregateType, ID id) {

        final String cacheKey = getKeyPrefix(aggregateType) + id;

        Map<byte[], byte[]> entities = execute(new RetryCallback<Map<byte[], byte[]>>() {
            @Override
            public Map<byte[], byte[]> doWithRetry(RetryContext context) {

                try (RedisCommands redisCommands = getRedisCommands()) {

                    return redisCommands.hgetAll(cacheKey.getBytes());

                } catch (IOException e) {
                    logger.error("findOne from cache failed.", e);
                }
                return null;
            }
        });

        if (!CollectionUtils.isEmpty(entities)) {

            return valueSerializer.deserialize(entities.get(Collections.max(entities.keySet(), new Comparator<byte[]>() {
                @Override
                public int compare(byte[] o1, byte[] o2) {
                    return Long.valueOf(new String(o1)).compareTo(Long.valueOf(new String(o2)));
                }
            })));
        }

        return null;
    }

    @Override
    public Collection<T> findAll(final Class<T> aggregateType, final Collection<ID> ids) {

        List<Object> result = execute(new RetryCallback<List<Object>>() {

            @Override
            public List<Object> doWithRetry(RetryContext context) {

                try (RedisCommands redisCommands = getRedisCommands()) {

                    return redisCommands.executePipelined(new CommandCallback<List<Object>>() {
                        @Override
                        public List<Object> execute(RedisCommands commands) {

                            List<Object> result = new ArrayList<>();

                            for (ID id : ids) {
                                result.add(commands.hgetAll((getKeyPrefix(aggregateType) + id).getBytes()));
                            }
                            return result;
                        }
                    });

                } catch (IOException e) {
                    logger.error("findAll from cache failed.", e);
                }
                return null;
            }
        });

        List<T> fetchedEntities = new ArrayList<T>();

        if (!CollectionUtils.isEmpty(result)) {
            for (Object object : result) {

                Map<byte[], byte[]> entities = (Map<byte[], byte[]>) object;

                if (!CollectionUtils.isEmpty(entities)) {

                    T entity = valueSerializer.deserialize(entities.get(Collections.max(entities.keySet(), new Comparator<byte[]>() {
                        @Override
                        public int compare(byte[] o1, byte[] o2) {
                            return Long.valueOf(new String(o1)).compareTo(Long.valueOf(new String(o2)));
                        }
                    })));

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

                    logger.warn("Redis operations failed but can be ignored", context.getLastThrowable());
                    return null;
                }

                throw new RuntimeException(context.getLastThrowable());
            }
        });
    }
}
