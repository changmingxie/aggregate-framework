package org.aggregateframework.transaction.server.dao;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.aggregateframework.transaction.repository.helper.JedisCallback;
import org.aggregateframework.transaction.repository.helper.RedisHelper;
import org.aggregateframework.transaction.server.model.Transaction;
import redis.clients.jedis.*;

import java.util.*;
import java.util.function.Supplier;

/**
 * Created by Lee on 2020/10/29 12:16.
 * aggregate-framework
 */

@Slf4j
public class RedisClusterTransactionDao implements TransactionDao {
    
    static final String RESET_SCRIPT = "if redis.call('exists',KEYS[1]) == 1 then redis.call('hset',KEYS[1],'RETRIED_COUNT',unpack(ARGV)); return '1'; end; return '0';";

    private final JedisCluster cluster;
    private final JedisPool    pool;
    private final String       id;

    private static final String DELETE_KEY_PREFIX = "DELETE:";
    private static final int    DELETE_KEY_KEEP_TIME = 3 * 24 * 3600;
    
    public RedisClusterTransactionDao(Supplier<JedisCluster> cluster,
                                      Supplier<JedisPool> pool,
                                      String id) {
        this.pool    = pool.get();
        this.id      = id;
        this.cluster = cluster.get();
        
    }
    
    @Override
    public List<Transaction> find(Integer pageNum, int pageSize) {
        return findByPattern(pageNum, pageSize, getDomain() + "*");
    }

    private List<Transaction> findByPattern(Integer pageNum, int pageSize, String pattern) {
        List<String> keys = RedisHelper.execute(pool, new JedisCallback<List<String>>() {
            @Override
            public List<String> doInJedis(Jedis jedis) {
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

                return allKeys.subList(start, end);
            }
        });

        List<Transaction> result = new ArrayList<>();

        for (String k : keys) {

            Map<String, String> d = cluster.hgetAll(k);

            if (d != null && !d.isEmpty()) {
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
                result.add(transaction);
            }
        }

        return result;
    }

    @Override
    public List<Transaction> findDeleted(Integer pageNum, int pageSize) {
        return findByPattern(pageNum, pageSize, DELETE_KEY_PREFIX + "{" + getDomain() + "*");
    }

    @Override
    public int count() {
        return countByPattern(getDomain() + "*");
    }

    @Override
    public int countDeleted() {
        return countByPattern(DELETE_KEY_PREFIX + "{" + getDomain() + "*");
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
    public boolean reset(String globalTxId, String branchQualifier) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void reset(List<String> keys) {
        for (String key : keys) {
            cluster.eval(RESET_SCRIPT, Lists.newArrayList(key), Lists.newArrayList("0"));
        }
    }

    @Override
    public void delete(List<String> keys) {
        for (String key : keys) {
            if(key.startsWith(DELETE_KEY_PREFIX)){
                cluster.del(key);
                return;
            }else {
                String delKeyName = DELETE_KEY_PREFIX + "{" + key + "}";
                cluster.renamenx(key, delKeyName);
                cluster.expire(delKeyName, DELETE_KEY_KEEP_TIME);
            }
        }
    }

    @Override
    public void restore(List<String> keys) {
        for (String key : keys) {
            if (!key.startsWith(DELETE_KEY_PREFIX)) {
                continue;
            }
            //额外需要去除{}
            String restoreKeyName = key.substring(DELETE_KEY_PREFIX.length() + 1, key.length() - 1);
            cluster.renamenx(key, restoreKeyName);
            cluster.persist(restoreKeyName);
        }
    }


    @Override
    public String getDomain() {
        return id;
    }

    public JedisCluster getCluster() {
        return cluster;
    }
    
    @Override
    public void close() throws Exception {
        if (cluster != null) {
            cluster.close();
        }
        if (pool != null) {
            pool.close();
        }
    
    }
}
