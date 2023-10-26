package org.aggregateframework.persistent.redis;

import redis.clients.jedis.ShardedJedis;

public interface ShardedJedisCallback<T> {
    public T doInJedis(ShardedJedis jedis);
}
