package org.aggregateframework.cache.helper;

import org.aggregateframework.transaction.repository.helper.CommandCallback;
import org.aggregateframework.transaction.repository.helper.RedisCommands;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RedisTemplateCommands implements RedisCommands {

    private RedisTemplate redisTemplate;

    public RedisTemplateCommands(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Object eval(byte[] scripts, List<byte[]> keys, List<byte[]> args) {
        throw new UnsupportedOperationException("eval in RedisTemplate unsupport right now!");
    }

    @Override
    public Long del(byte[] key) {
        this.redisTemplate.delete(key);
        return 1l;
    }

    @Override
    public void set(byte[] key, byte[] value) {
        this.redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public byte[] get(byte[] key) {
        return (byte[]) this.redisTemplate.opsForValue().get(key);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return this.redisTemplate.opsForHash().entries(key);
    }

    @Override
    public void hset(byte[] key, byte[] field, byte[] value) {
        this.redisTemplate.opsForHash().put(key, field, value);
    }

    @Override
    public void hdel(byte[] key, byte[] field) {
        this.redisTemplate.opsForHash().delete(key, field);
    }

    @Override
    public void expire(byte[] key, int expireTime) {
        this.redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
    }

    @Override
    public List<Object> executePipelined(CommandCallback commandCallback) {

        return redisTemplate.executePipelined(new SessionCallback<Object>() {

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                return commandCallback.execute(new RedisCommands() {
                    @Override
                    public Object eval(byte[] scripts, List<byte[]> keys, List<byte[]> args) {
                        throw new UnsupportedOperationException("eval in RedisOperation unsupport right now!");
                    }

                    @Override
                    public Long del(byte[] key) {
                        operations.delete(key);
                        return null;
                    }

                    @Override
                    public void set(byte[] key, byte[] value) {
                        operations.opsForValue().set(key, value);
                    }

                    @Override
                    public byte[] get(byte[] key) {
                        return (byte[]) operations.opsForValue().get(key);
                    }

                    @Override
                    public Map<byte[], byte[]> hgetAll(byte[] key) {
                        return null;
                    }

                    @Override
                    public void hset(byte[] key, byte[] field, byte[] value) {
                        operations.opsForHash().put(key, field, value);
                    }

                    @Override
                    public void hdel(byte[] key, byte[] field) {
                        operations.opsForHash().delete(key, field);
                    }

                    @Override
                    public void expire(byte[] key, int expireTime) {
                        operations.expire(key, expireTime, TimeUnit.SECONDS);
                    }

                    @Override
                    public List<Object> executePipelined(CommandCallback commandCallback) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void close() throws IOException {

                    }
                });
            }
        });
    }

    @Override
    public void close() throws IOException {

    }
}
