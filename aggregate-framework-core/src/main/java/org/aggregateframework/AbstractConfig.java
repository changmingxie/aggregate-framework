package org.aggregateframework;

import io.openmessaging.storage.dledger.proxy.DLedgerProxy;
import org.aggregateframework.discovery.registry.RegistryConfig;
import org.aggregateframework.discovery.registry.RegistryType;
import org.aggregateframework.discovery.registry.direct.DirectRegistryProperties;
import org.aggregateframework.discovery.registry.nacos.NacosRegistryProperties;
import org.aggregateframework.discovery.registry.zookeeper.ZookeeperRegistryProperties;
import org.aggregateframework.properties.RecoveryProperties;
import org.aggregateframework.properties.registry.RegistryProperties;
import org.aggregateframework.properties.remoting.NettyProperties;
import org.aggregateframework.properties.store.StoreProperties;
import org.aggregateframework.recovery.RecoveryConfig;
import org.aggregateframework.remoting.netty.NettyConfig;
import org.aggregateframework.transaction.serializer.SerializerType;
import org.aggregateframework.storage.StorageType;
import org.aggregateframework.storage.StoreConfig;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedisPool;

import javax.sql.DataSource;
import java.util.Map;

public class AbstractConfig implements StoreConfig, RecoveryConfig, NettyConfig, RegistryConfig {

    //StoreConfig
    private StoreConfig storeConfig = new StoreProperties();
    //RecoveryConfig
    private RecoveryConfig recoveryConfig = new RecoveryProperties();
    //NettyConfig
    private NettyConfig nettyConfig = new NettyProperties();
    //RegistyConfig
    private RegistryConfig registryConfig = new RegistryProperties();

    public AbstractConfig() {
    }

    public AbstractConfig(StoreConfig storeConfig, RecoveryConfig recoveryConfig, NettyConfig nettyConfig, RegistryConfig registryConfig) {
        if (storeConfig != null) {
            this.storeConfig = storeConfig;
        }
        if (recoveryConfig != null) {
            this.recoveryConfig = recoveryConfig;
        }
        if (nettyConfig != null) {
            this.nettyConfig = nettyConfig;
        }
        if (recoveryConfig != null) {
            this.registryConfig = registryConfig;
        }
    }

    @Override
    public String getTbSuffix() {
        return storeConfig.getTbSuffix();
    }

    @Override
    public DataSource getDataSource() {
        return storeConfig.getDataSource();
    }

    @Override
    public String getDomain() {
        return storeConfig.getDomain();
    }

    @Override
    public StorageType getStorageType() {
        return storeConfig.getStorageType();
    }

    @Override
    public SerializerType getSerializerType() {
        return storeConfig.getSerializerType();
    }

    @Override
    public String getTransactionSerializerClassName() {
        return storeConfig.getTransactionSerializerClassName();
    }

    @Override
    public int getKryoPoolSize() {
        return storeConfig.getKryoPoolSize();
    }

    @Override
    public int getMaxTransactionSize() {
        return storeConfig.getMaxTransactionSize();
    }

    @Override
    public int getMaxAttempts() {
        return storeConfig.getMaxAttempts();
    }

    @Override
    public Map<String, Object> getCustomStorageProperties() {
        return storeConfig.getCustomStorageProperties();
    }

    @Override
    public String getTransactionStorageClass() {
        return storeConfig.getTransactionStorageClass();
    }

    @Override
    public long getRequestTimeoutMillis() {
        return storeConfig.getRequestTimeoutMillis();
    }

    @Override
    public String getLocation() {
        return storeConfig.getLocation();
    }

    @Override
    public JedisPool getJedisPool() {
        return storeConfig.getJedisPool();
    }

    @Override
    public JedisCluster getJedisCluster() {
        return storeConfig.getJedisCluster();
    }

    @Override
    public DLedgerProxy getDLedgerProxy() {
        return storeConfig.getDLedgerProxy();
    }

    @Override
    public ShardedJedisPool getShardedJedisPool() {
        return storeConfig.getShardedJedisPool();
    }

    @Override
    public String getRemoteCluster() {
        return storeConfig.getRemoteCluster();
    }

    @Override
    public int getRequestProcessThreadSize() {
        return nettyConfig.getRequestProcessThreadSize();
    }

    @Override
    public int getRequestProcessThreadQueueCapacity() {
        return nettyConfig.getRequestProcessThreadQueueCapacity();
    }

    @Override
    public int getMaxRetryCount() {
        return recoveryConfig.getMaxRetryCount();
    }

    @Override
    public int getRecoverDuration() {
        return recoveryConfig.getRecoverDuration();
    }

    @Override
    public String getCronExpression() {
        return recoveryConfig.getCronExpression();
    }

    @Override
    public int getFetchPageSize() {
        return recoveryConfig.getFetchPageSize();
    }

    @Override
    public int getConcurrentRecoveryThreadCount() {
        return recoveryConfig.getConcurrentRecoveryThreadCount();
    }

    @Override
    public boolean isRecoveryEnabled() {
        return recoveryConfig.isRecoveryEnabled();
    }

    @Override
    public String getQuartzDataSourceDriver() {
        return recoveryConfig.getQuartzDataSourceDriver();
    }

    @Override
    public String getQuartzDataSourceUrl() {
        return recoveryConfig.getQuartzDataSourceUrl();
    }

    @Override
    public String getQuartzDataSourceUser() {
        return recoveryConfig.getQuartzDataSourceUser();
    }

    @Override
    public String getQuartzDataSourcePassword() {
        return recoveryConfig.getQuartzDataSourcePassword();
    }

    @Override
    public String getQuartzDataSourceValidationQuery() {
        return recoveryConfig.getQuartzDataSourceValidationQuery();
    }

    @Override
    public int getQuartzDataSourceCheckoutTimeout() {
        return recoveryConfig.getQuartzDataSourceCheckoutTimeout();
    }

    @Override
    public int getQuartzDataSourceInitialPoolSize() {
        return recoveryConfig.getQuartzDataSourceInitialPoolSize();
    }

    @Override
    public int getQuartzDataSourceMinPoolSize() {
        return recoveryConfig.getQuartzDataSourceMinPoolSize();
    }

    @Override
    public int getQuartzDataSourceMaxPoolSize() {
        return recoveryConfig.getQuartzDataSourceMaxPoolSize();
    }

    @Override
    public int getQuartzThreadPoolThreadCount() {
        return recoveryConfig.getQuartzThreadPoolThreadCount();
    }

    @Override
    public boolean isQuartzClustered() {
        return recoveryConfig.isQuartzClustered();
    }

    @Override
    public String getCustomConnectionProviderClassName() {
        return recoveryConfig.getCustomConnectionProviderClassName();
    }

    @Override
    public Map<String, String> getCustomConnectionProviderProperties() {
        return recoveryConfig.getCustomConnectionProviderProperties();
    }

    @Override
    public boolean isUpdateJobForcibly() {
        return recoveryConfig.isUpdateJobForcibly();
    }

    @Override
    public int getWorkerThreadSize() {
        return nettyConfig.getWorkerThreadSize();
    }

    @Override
    public int getSocketBacklog() {
        return nettyConfig.getSocketBacklog();
    }

    @Override
    public int getSocketRcvBufSize() {
        return nettyConfig.getSocketRcvBufSize();
    }

    @Override
    public int getSocketSndBufSize() {
        return nettyConfig.getSocketSndBufSize();
    }

    @Override
    public int getFrameMaxLength() {
        return nettyConfig.getFrameMaxLength();
    }

    @Override
    public int getWorkSelectorThreadSize() {
        return nettyConfig.getWorkSelectorThreadSize();
    }

    @Override
    public String getClusterName() {
        return registryConfig.getClusterName();
    }

    @Override
    public ZookeeperRegistryProperties getZookeeperRegistryProperties() {
        return registryConfig.getZookeeperRegistryProperties();
    }

    @Override
    public NacosRegistryProperties getNacosRegistryProperties() {
        return registryConfig.getNacosRegistryProperties();
    }

    @Override
    public DirectRegistryProperties getDirectRegistryProperties() {
        return registryConfig.getDirectRegistryProperties();
    }

    @Override
    public RegistryType getRegistryType() {
        return registryConfig.getRegistryType();
    }

    @Override
    public String getCustomRegistryName() {
        return registryConfig.getCustomRegistryName();
    }

    public void setStoreConfig(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    public void setRecoveryConfig(RecoveryConfig recoveryConfig) {
        this.recoveryConfig = recoveryConfig;
    }

    protected void setNettyConfig(NettyConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
    }

    protected void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }
}
