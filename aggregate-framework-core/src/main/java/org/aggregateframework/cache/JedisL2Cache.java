package org.aggregateframework.cache;

import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.transaction.repository.helper.JedisCommands;
import org.aggregateframework.transaction.repository.helper.RedisCommands;
import redis.clients.jedis.JedisPool;

import java.io.Serializable;

public class JedisL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> extends AbstractRedisL2Cache<T, ID> {

    private JedisPool jedisPool;


    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    @Override
    public RedisCommands getRedisCommands() {
        return new JedisCommands(jedisPool.getResource());
    }

}
