package org.aggregateframework.sample.quickstart.command.domain.repository;

import org.aggregateframework.serializer.KryoPoolSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

public class KryoRedisSerializer extends KryoPoolSerializer implements RedisSerializer<Object> {

}
