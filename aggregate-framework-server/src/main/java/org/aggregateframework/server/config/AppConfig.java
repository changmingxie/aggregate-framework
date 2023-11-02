package org.aggregateframework.server.config;

import org.aggregateframework.AggServer;
import org.aggregateframework.ServerConfig;
import org.aggregateframework.discovery.registry.ServerRegistryConfig;
import org.aggregateframework.properties.RecoveryProperties;
import org.aggregateframework.properties.registry.ServerRegistryProperties;
import org.aggregateframework.properties.remoting.NettyServerProperties;
import org.aggregateframework.properties.store.StoreProperties;
import org.aggregateframework.recovery.RecoveryConfig;
import org.aggregateframework.remoting.netty.NettyServerConfig;
import org.aggregateframework.spring.factory.SpringBeanFactory;
import org.aggregateframework.storage.StoreConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class AppConfig {

    @Bean
    @ConfigurationProperties("spring.agg.remoting")
    public NettyServerProperties nettyServerProperties() {
        return new NettyServerProperties();
    }

    @Bean
    @ConfigurationProperties("spring.agg.storage")
    public StoreProperties storeProperties() {
        return new StoreProperties();
    }

    @Bean
    @ConfigurationProperties("spring.agg.registry")
    public ServerRegistryProperties registryProperties() {
        return new ServerRegistryProperties();
    }

    @Bean
    @ConfigurationProperties("spring.agg.recovery")
    public RecoveryProperties recoveryProperties() {
        return new RecoveryProperties();
    }

    @Bean
    public ServerConfig serverConfig(@Autowired ServerRegistryConfig serverRegistryConfig,
                                     @Autowired StoreConfig storeConfig,
                                     @Autowired RecoveryConfig recoveryConfig,
                                     @Autowired NettyServerConfig nettyServerConfig) {
        return new ServerConfig(storeConfig, recoveryConfig, nettyServerConfig, serverRegistryConfig);
    }

    @Bean
    public AggServer aggServer(@Autowired ServerConfig serverConfig) {
        return new AggServer(serverConfig);
    }

    @Bean
    public SpringBeanFactory springBeanFactory() {
        return new SpringBeanFactory();
    }
}
