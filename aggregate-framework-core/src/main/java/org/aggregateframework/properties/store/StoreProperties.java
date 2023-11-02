package org.aggregateframework.properties.store;

import io.openmessaging.storage.dledger.proxy.DLedgerProxy;
import org.aggregateframework.transaction.serializer.SerializerType;
import org.aggregateframework.storage.StorageType;
import org.aggregateframework.storage.StoreConfig;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedisPool;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nervose.Wu
 * @date 2022/5/24 09:45
 */
public class StoreProperties implements StoreConfig {

    private StorageType storageType = StorageType.MEMORY;
    //only used by client
    private SerializerType serializerType = SerializerType.KRYO;
    //only used by client
    private String transactionSerializerClassName;
    private int kryoPoolSize = 512;
    private int maxTransactionSize = 1 * 1024 * 1024; // 1M
    //only used by client
    private int maxAttempts = 2;
    private String domain = "AGG";
    private String transactionStorageClass;
    //the timout of remoting storage request
    private long requestTimeoutMillis = 2000L;
    private String location = "/tmp";
    private String tbSuffix;
    // only used by client & storageType is REMOTING
    private String remoteCluster = "default";
    private JdbcStoreProperties jdbc = new JdbcStoreProperties();
    private RedisStoreProperties redis = new RedisStoreProperties();
    private ShardRedisStoreProperties shardRedis = new ShardRedisStoreProperties();
    private RedisClusterStoreProperties redisCluster = new RedisClusterStoreProperties();

    private DLedgerProperties dledger = new DLedgerProperties();

    private Map<String,Object> customStorageProperties = new HashMap<>();

    @Override
    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    @Override
    public SerializerType getSerializerType() {
        return serializerType;
    }

    public void setSerializerType(SerializerType serializerType) {
        this.serializerType = serializerType;
    }

    @Override
    public String getTransactionSerializerClassName() {
        return transactionSerializerClassName;
    }

    public void setTransactionSerializerClassName(String transactionSerializerClassName) {
        this.transactionSerializerClassName = transactionSerializerClassName;
    }

    @Override
    public int getKryoPoolSize() {
        return kryoPoolSize;
    }

    public void setKryoPoolSize(int kryoPoolSize) {
        this.kryoPoolSize = kryoPoolSize;
    }

    @Override
    public int getMaxTransactionSize() {
        return maxTransactionSize;
    }

    public void setMaxTransactionSize(int maxTransactionSize) {
        this.maxTransactionSize = maxTransactionSize;
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getTransactionStorageClass() {
        return transactionStorageClass;
    }

    public void setTransactionStorageClass(String transactionStorageClass) {
        this.transactionStorageClass = transactionStorageClass;
    }

    @Override
    public long getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }

    public void setRequestTimeoutMillis(long requestTimeoutMillis) {
        this.requestTimeoutMillis = requestTimeoutMillis;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getTbSuffix() {
        return tbSuffix;
    }

    public void setTbSuffix(String tbSuffix) {
        this.tbSuffix = tbSuffix;
    }

    @Override
    public String getRemoteCluster() {
        return remoteCluster;
    }

    public void setRemoteCluster(String remoteCluster) {
        this.remoteCluster = remoteCluster;
    }

    public JdbcStoreProperties getJdbc() {
        return jdbc;
    }

    public void setJdbc(JdbcStoreProperties jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public JedisPool getJedisPool() {
        return redis.getJedisPool();
    }

    @Override
    public ShardedJedisPool getShardedJedisPool() {
        return shardRedis.getShardedJedisPool();
    }

    @Override
    public JedisCluster getJedisCluster() {
        return redisCluster.getCluster();
    }

    @Override
    public DLedgerProxy getDLedgerProxy() {
        //return dledger.getDLedgerProxy();
        return null;
    }

    @Override
    public DataSource getDataSource() {
        return jdbc.getDataSource();
    }

    public RedisStoreProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisStoreProperties redis) {
        this.redis = redis;
    }

    public ShardRedisStoreProperties getShardRedis() {
        return shardRedis;
    }

    public void setShardRedis(ShardRedisStoreProperties shardRedis) {
        this.shardRedis = shardRedis;
    }

    public RedisClusterStoreProperties getRedisCluster() {
        return redisCluster;
    }

    public void setRedisCluster(RedisClusterStoreProperties redisCluster) {
        this.redisCluster = redisCluster;
    }

    public DLedgerProperties getDledger() {
        return dledger;
    }

    public void setDledger(DLedgerProperties dledger) {
        this.dledger = dledger;
    }

    @Override
    public Map<String, Object> getCustomStorageProperties() {
        return customStorageProperties;
    }

    public void setCustomStorageProperties(Map<String, Object> customStorageProperties) {
        this.customStorageProperties = customStorageProperties;
    }
}
