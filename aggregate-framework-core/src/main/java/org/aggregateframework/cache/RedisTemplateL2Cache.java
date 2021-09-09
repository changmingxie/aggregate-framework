package org.aggregateframework.cache;

import org.aggregateframework.cache.helper.RedisTemplateCommands;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.transaction.repository.helper.RedisCommands;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;

/**
 * Created by changming.xie on 9/17/17.
 */
public class RedisTemplateL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> extends AbstractRedisL2Cache<T, ID> {

    private RedisTemplate redisTemplate;

    @Override
    public RedisCommands getRedisCommands() {
        return new RedisTemplateCommands(redisTemplate);
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
