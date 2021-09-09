package org.aggregateframework.cache;

import io.codis.jodis.JedisResourcePool;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.transaction.repository.helper.JedisCommands;
import org.aggregateframework.transaction.repository.helper.RedisCommands;

import java.io.Serializable;

@Deprecated
public class JodisL2Cache<T extends AggregateRoot<ID>, ID extends Serializable> extends AbstractRedisHashL2Cache<T, ID> {

    private JedisResourcePool jedisPool;

    @Override
    public RedisCommands getRedisCommands() {
        return new JedisCommands(jedisPool.getResource());
    }

    public void setJedisPool(JedisResourcePool jedisPool) {
        this.jedisPool = jedisPool;
    }
}
