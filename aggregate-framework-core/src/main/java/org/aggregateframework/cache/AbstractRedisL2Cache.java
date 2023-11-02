package org.aggregateframework.cache;

import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.persistent.redis.CommandCallback;
import org.aggregateframework.persistent.redis.RedisCommands;
import org.aggregateframework.retry.*;
import org.aggregateframework.serializer.ObjectSerializer;
import org.aggregateframework.serializer.RegisterableKryoSerializer;
import org.aggregateframework.utils.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractRedisL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> implements L2Cache<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRedisL2Cache.class);

    private static final String DEFAULT_KEY_PREFIX = "AGG:L2CACHEV3:";

    protected ObjectSerializer<T> valueSerializer = new RegisterableKryoSerializer();

    protected int maxAttempts = 3;

    protected int expireTimeInSecond = 60 * 30; //30 minute

    protected String keyPrefix = null;

    protected boolean suppressErrors = true;


    public ObjectSerializer<T> getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(ObjectSerializer<T> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public void setExpireTimeInSecond(int expireTimeInSecond) {
        this.expireTimeInSecond = expireTimeInSecond;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public void setSuppressErrors(boolean suppressErrors) {
        this.suppressErrors = suppressErrors;
    }


    protected String getKeyPrefix(Class aggregateType) {

        String keyPrefix = getKeyPrefix();

        if (StringUtils.isEmpty(keyPrefix)) {
            return DEFAULT_KEY_PREFIX + aggregateType.getName() + ":";
        } else {
            return keyPrefix;
        }
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

                            doRemove(commands, entities);
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

                            doWrite(commands, entities);
                            return null;
                        }
                    });
                } catch (
                        IOException e) {
                    logger.error("write cache failed.", e);
                }

                return null;
            }
        });
    }

    @Override
    public T findOne(Class<T> aggregateType, ID id) {

        final String cacheKey = getKeyPrefix(aggregateType) + id;

        byte[] entity = execute(new RetryCallback<byte[]>() {
            @Override
            public byte[] doWithRetry(RetryContext context) {

                try (RedisCommands redisCommands = getRedisCommands()) {

                    return redisCommands.get(cacheKey.getBytes());

                } catch (IOException e) {
                    logger.error("findOne from cache failed.", e);
                }
                return null;
            }
        });

        if (entity != null && entity.length > 0) {
            return valueSerializer.deserialize(entity);
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

                            for (ID id : ids) {
                                commands.get((getKeyPrefix(aggregateType) + id).getBytes());
                            }
                            return null;
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

                byte[] entityBytes = (byte[]) object;

                if (entityBytes != null && entityBytes.length > 0) {

                    T entity = valueSerializer.deserialize(entityBytes);

                    fetchedEntities.add(entity);
                }
            }
        }

        return fetchedEntities;
    }

    protected void doRemove(RedisCommands commands, Collection<T> entities) {
        for (T entity : entities) {
            commands.del((getKeyPrefix(entity.getClass()) + entity.getId()).getBytes());
        }
    }

    protected void doWrite(RedisCommands commands, Collection<T> entities) {
        for (T entity : entities) {
            String cacheKey = getKeyPrefix(entity.getClass()) + entity.getId();
            commands.set(cacheKey.getBytes(), valueSerializer.serialize(entity));
            commands.expire(cacheKey.getBytes(), expireTimeInSecond);
        }
    }

    protected <T> T execute(RetryCallback<T> retryCallback) {

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
