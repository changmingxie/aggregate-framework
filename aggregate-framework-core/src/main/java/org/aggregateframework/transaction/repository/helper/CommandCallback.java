package org.aggregateframework.transaction.repository.helper;

public interface CommandCallback<T> {
    T execute(RedisCommands commands);
}
