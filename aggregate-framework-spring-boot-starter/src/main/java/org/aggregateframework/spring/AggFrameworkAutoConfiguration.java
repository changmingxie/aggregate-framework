package org.aggregateframework.spring;


import org.aggregateframework.ClientConfig;
import org.aggregateframework.properties.RecoveryProperties;
import org.aggregateframework.properties.registry.ClientRegistryProperties;
import org.aggregateframework.properties.remoting.NettyClientProperties;
import org.aggregateframework.properties.store.StoreProperties;
import org.aggregateframework.recovery.RecoveryConfig;
import org.aggregateframework.remoting.netty.NettyClientConfig;
import org.aggregateframework.spring.annotation.EnableSpringIntegration;
import org.aggregateframework.storage.StoreConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author Nervose.Wu
 * @date 2022/5/26 11:48
 */
@EnableSpringIntegration
@EnableConfigurationProperties
public class AggFrameworkAutoConfiguration {

    @Bean
    @ConfigurationProperties("spring.agg.remoting")
    public NettyClientProperties nettyClientProperties() {
        return new NettyClientProperties();
    }

    @Bean
    @ConfigurationProperties("spring.agg.storage")
    public StoreProperties storeProperties() {
        return new StoreProperties();
    }

    @Bean
    @ConfigurationProperties("spring.agg.registry")
    public ClientRegistryProperties registryProperties() {
        return new ClientRegistryProperties();
    }

    @Bean
    @ConfigurationProperties("spring.agg.recovery")
    public RecoveryProperties recoveryProperties() {
        return new RecoveryProperties();
    }

    @Bean
    public ClientConfig clientConfig(@Autowired ClientRegistryProperties clientRegistryProperties,
                                     @Autowired StoreConfig storeConfig,
                                     @Autowired RecoveryConfig recoveryConfig,
                                     @Autowired NettyClientConfig nettyClientConfig) {
        return new ClientConfig(storeConfig, recoveryConfig, nettyClientConfig, clientRegistryProperties);
    }
}
