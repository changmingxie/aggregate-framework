package org.aggregateframework.cache;

import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.transaction.repository.helper.RedisCommands;
import org.aggregateframework.transaction.repository.helper.ShardJedisCommands;
import redis.clients.jedis.ShardedJedisPool;

import java.io.Serializable;

/**
 * Created by changming.xie on 9/17/17.
 */
public class ShardJedisL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> extends AbstractRedisL2Cache<T, ID> {

    private ShardedJedisPool shardedJedisPool;

    @Override
    public RedisCommands getRedisCommands() {
        return new ShardJedisCommands(shardedJedisPool.getResource());
    }

    public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
    }
}
