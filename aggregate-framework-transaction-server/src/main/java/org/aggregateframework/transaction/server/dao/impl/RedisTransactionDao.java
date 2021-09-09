package org.aggregateframework.transaction.server.dao.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.aggregateframework.transaction.repository.helper.JedisCallback;
import org.aggregateframework.transaction.repository.helper.RedisHelper;
import org.aggregateframework.transaction.server.S;
import org.aggregateframework.transaction.server.dao.TransactionDao;
import org.aggregateframework.transaction.server.model.Transaction;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Created by changming.xie on 9/7/16.
 */

@Slf4j
public class RedisTransactionDao implements TransactionDao {
    
    
    private final String    id;
    private final JedisPool pool;

    private static final String DELETE_KEY_PREFIX = "DELETE:";
    private static final int    DELETE_KEY_KEEP_TIME = 3 * 24 * 3600;

    public RedisTransactionDao(String id,
                               String host,
                               int port,
                               int database,
                               String password) {
        
        this.id = id;
        
        this.pool = create(id, host, port, password, database);
    }
    
    private JedisPool create(String domain, String host, int port, String password, int database) {
        JedisPool pool = new JedisPool(new GenericObjectPoolConfig<>(), host, port, 10000, password, database, domain);
        try (Jedis jedis = pool.getResource()) {
            jedis.info();
        }
        return pool;
    }
    
    
    @Override
    public List<Transaction> find(final Integer pageNum, final int pageSize) {
        
        return findByPattern(pageNum, pageSize,getDomain() + "*");
    }

    @Override
    public List<Transaction> findDeleted(Integer pageNum, int pageSize) {
        return findByPattern(pageNum,pageSize,DELETE_KEY_PREFIX + getDomain()+"*");
    }

    private List<Transaction> findByPattern(Integer pageNum, int pageSize, final String pattern) {
        return RedisHelper.execute(pool, new JedisCallback<List<Transaction>>() {
            @Override
            public List<Transaction> doInJedis(Jedis jedis) {

                int start = (pageNum - 1) * pageSize;
                int end = pageNum * pageSize;

                ArrayList<String> allKeys = new ArrayList<>();

                String cursor = RedisHelper.SCAN_INIT_CURSOR;
                ScanParams scanParams = RedisHelper.scanArgs(pattern, RedisHelper.SCAN_COUNT);
                do {
                    ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                    allKeys.addAll(scanResult.getResult());
                    cursor = scanResult.getCursor();
                } while (!cursor.equals(RedisHelper.SCAN_INIT_CURSOR) && allKeys.size() < end);


                if (allKeys.size() < start) {
                    return Collections.emptyList();
                }

                if (end > allKeys.size()) {
                    end = allKeys.size();
                }

                final List<String> keys = allKeys.subList(start, end);

                Pipeline pipeline = jedis.pipelined();

                for (final String key : keys) {
                    pipeline.hgetAll(key);
                }
                List<Object> result = pipeline.syncAndReturnAll();
                final List<Map<String, String>> data = result.stream()
                                                             .map(o -> (Map<String, String>) o)
                                                             .collect(Collectors.toList());


                return S.zip(keys.stream(), data.stream(), new BiFunction<String, Map<String, String>, Transaction>() {
                    @Override
                    public Transaction apply(String k, Map<String, String> d) {
                        if (!d.isEmpty()) {
                            String global_tx_id = UUID.nameUUIDFromBytes(d.get("GLOBAL_TX_ID").getBytes()).toString();
                            String branch_qualifier = UUID.nameUUIDFromBytes(d.get("BRANCH_QUALIFIER").getBytes()).toString();

                            Transaction transaction = new Transaction();
                            transaction.setGlobalTxId(global_tx_id);
                            transaction.setBranchQualifier(branch_qualifier);
                            transaction.setStatus(d.get("STATUS"));
                            transaction.setType(d.get("TRANSACTION_TYPE"));
                            transaction.setRetried(d.get("RETRIED_COUNT"));
                            transaction.setCreateTime(d.get("CREATE_TIME"));
                            transaction.setLastUpdateTime(d.get("LAST_UPDATE_TIME"));
                            transaction.setContent(d.get("CONTENT_VIEW"));
                            transaction.setKey(k);
                            transaction.setDomain(id);
                            return transaction;
                        } else {
                            return null;
                        }
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
            }
        });
    }


    @Override
    public int count() {
        return countByPattern(getDomain() + "*");
    }

    @Override
    public int countDeleted() {
        return countByPattern(DELETE_KEY_PREFIX + getDomain() + "*");
    }

    private int countByPattern(String pattern) {
        return RedisHelper.execute(pool, new JedisCallback<Integer>() {
            @Override
            public Integer doInJedis(Jedis jedis) {
                int count = 0;
                String cursor = RedisHelper.SCAN_INIT_CURSOR;
                ScanParams scanParams = RedisHelper.scanArgs(pattern, RedisHelper.SCAN_COUNT);
                do {
                    ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                    count += scanResult.getResult().size();
                    cursor = scanResult.getCursor();
                } while (!cursor.equals(RedisHelper.SCAN_INIT_CURSOR));
                return count;
            }
        });
    }

    @Override
    @Deprecated
    public boolean reset(final String globalTxId, final String branchQualifier) {
        
        return RedisHelper.execute(pool, new JedisCallback<Boolean>() {
            @Override
            public Boolean doInJedis(Jedis jedis) {
                
                byte[] key = RedisHelper.getRedisKey(getDomain(), globalTxId, branchQualifier);
                
                Long result = jedis.hset(key, "RETRIED_COUNT".getBytes(), "0".getBytes());
                return result > 0;
            }
        });
    }
    
    @Override
    public void reset(List<String> keys) {
    
        for (String key : keys) {
            Object rr = RedisHelper.execute(pool, new JedisCallback<Object>() {
                @Override
                public Object doInJedis(Jedis jedis) {
                    return jedis.eval(reset_script, Lists.newArrayList(key, "RETRIED_COUNT"), Lists.newArrayList("0"));
                
                }
            });
            log.info("!23123 {}", rr);
        }

//        List<Object> retried_count = RedisHelper.execute(pool, new JedisCallback<List<Object>>() {
//            @Override
//            public List<Object> doInJedis(Jedis jedis) {
//                Pipeline p = jedis.pipelined();
//                for (String key : keys) {
//
//                    p.eval(reset_script, Lists.newArrayList(key, "RETRIED_COUNT"), Lists.newArrayList("0"));
//                }
//
//                return p.syncAndReturnAll();
//            }
//        });
    
    
    }

    @Override
    public void delete(List<String> keys) {

        for (String key : keys) {
            RedisHelper.execute(pool, new JedisCallback<Boolean>() {
                @Override
                public Boolean doInJedis(Jedis jedis) {
                    if(key.startsWith(DELETE_KEY_PREFIX)){
                        return jedis.del(key) > 0;
                    }else {
                        String delKeyName = DELETE_KEY_PREFIX + key;
                        Long result = jedis.renamenx(key, delKeyName);
                        jedis.expire(delKeyName, DELETE_KEY_KEEP_TIME);
                        return result > 0;
                    }
                }
            });
        }
    }

    @Override
    public void restore(List<String> keys) {

        for (String key : keys) {
            RedisHelper.execute(pool, new JedisCallback<Boolean>() {
                @Override
                public Boolean doInJedis(Jedis jedis) {
                    if (!key.startsWith(DELETE_KEY_PREFIX)) {
                        return false;
                    }
                    String restoreKeyName = key.substring(DELETE_KEY_PREFIX.length());
                    Long result = jedis.renamenx(key, restoreKeyName);
                    jedis.persist(restoreKeyName);
                    return result > 0;
                }
            });
        }
    }


    @Override
    public void close() throws Exception {
        if (pool != null) {
            pool.close();
        }
    }
    
    @Override
    public String getDomain() {
        return id;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        RedisTransactionDao dao = (RedisTransactionDao) o;
        
        return id.equals(dao.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RedisTransactionDao{");
        sb.append("id='").append(id).append('\'');
        sb.append(", pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}
