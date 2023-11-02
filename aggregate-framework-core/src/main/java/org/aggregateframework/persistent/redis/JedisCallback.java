package org.aggregateframework.persistent.redis;

import redis.clients.jedis.Jedis;

/**
 * Created by changming.xie on 9/15/16.
 */
public interface JedisCallback<T> {

    T doInJedis(Jedis jedis);
}