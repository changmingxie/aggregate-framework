package org.aggregateframework.sample.quickstart.command.infrastructure.cache;

import org.aggregateframework.cache.JedisL2Cache;
import org.aggregateframework.persistent.redis.RedisCommands;
import org.aggregateframework.retry.RetryCallback;
import org.aggregateframework.retry.RetryContext;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;

import java.io.IOException;
import java.util.Collection;

public class PricedOrderL2Cache extends JedisL2Cache<PricedOrder, Long> {

    protected void doRemove(RedisCommands commands, Collection<PricedOrder> entities) {

        super.doRemove(commands, entities);

        for (PricedOrder entity : entities) {
//            commands.del((getKeyPrefix(entity.getClass()) + entity.getId()).getBytes());
        }

    }

    protected void doWrite(RedisCommands commands, Collection<PricedOrder> entities) {

        super.doWrite(commands, entities);

        for (PricedOrder entity : entities) {
//            String cacheKey = getKeyPrefix(entity.getClass()) + entity.getId();
//            commands.set(cacheKey.getBytes(), valueSerializer.serialize(entity));
//            commands.expire(cacheKey.getBytes(), expireTimeInSecond);
        }
    }

    public PricedOrder findByNo(String orderNo) {

        final String cacheKey = orderNo + "n0000";

        byte[] id = execute(new RetryCallback<byte[]>() {
            @Override
            public byte[] doWithRetry(RetryContext context) {

                try (RedisCommands redisCommands = getRedisCommands()) {

                    return redisCommands.get(cacheKey.getBytes());

                } catch (IOException e) {

                }
                return null;
            }
        });

        if (id != null && id.length > 0) {
            String idStr = new String(id);
            return findOne(PricedOrder.class, Long.valueOf(idStr));
        }

        return null;

    }
}
