package org.aggregateframework.cache;

import io.codis.jodis.JedisResourcePool;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.persistent.redis.JedisCommands;
import org.aggregateframework.persistent.redis.RedisCommands;

import java.io.Serializable;

public class JodisClusterL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> extends AbstractRedisL2Cache<T, ID> {

    private JedisResourcePool jedisPool;

    @Override
    public RedisCommands getRedisCommands() {
        return new JedisCommands(jedisPool.getResource());
    }

    public void setJedisPool(JedisResourcePool jedisPool) {
        this.jedisPool = jedisPool;
    }
}
