package org.aggregateframework.cache;

import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.persistent.redis.JedisClusterCommands;
import org.aggregateframework.persistent.redis.RedisCommands;
import redis.clients.jedis.JedisCluster;

import java.io.Serializable;

public class JedisClusterL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> extends AbstractRedisL2Cache<T, ID> {

    private JedisCluster jedisCluster;

    @Override
    public RedisCommands getRedisCommands() {
        return new JedisClusterCommands(jedisCluster);
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }
}
