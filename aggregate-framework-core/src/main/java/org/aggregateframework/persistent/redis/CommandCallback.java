package org.aggregateframework.persistent.redis;

public interface CommandCallback<T> {
    T execute(RedisCommands commands);
}
