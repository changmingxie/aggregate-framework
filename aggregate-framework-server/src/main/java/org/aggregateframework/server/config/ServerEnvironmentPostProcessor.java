package org.aggregateframework.server.config;

import org.aggregateframework.discovery.registry.RegistryType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Optional;
import java.util.Properties;

/**
 * @Author huabao.fang
 * @Date 2022/7/18 13:23
 **/
public class ServerEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private Logger logger = LoggerFactory.getLogger(ServerEnvironmentPostProcessor.class);

    private Properties aggServerProperties = new Properties();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        PropertySource<?> propertySource = selectApplicationConfigPropertySource(propertySources);
        if (propertySource != null) {// 读取application.yml文件内容来动态调整配置
            rebuildRegistryProperties(environment);
            PropertiesPropertySource aggServerPropertySource = new PropertiesPropertySource("aggServerProperties", aggServerProperties);
            propertySources.addLast(aggServerPropertySource);
        }
    }

    private PropertySource selectApplicationConfigPropertySource(MutablePropertySources propertySources) {
        Optional<PropertySource<?>> propertySourceOptional = propertySources.stream().filter(propertySource -> propertySource.getName().contains("applicationConfig")).findFirst();
        return propertySourceOptional.orElse(null);
    }

    private void rebuildRegistryProperties(ConfigurableEnvironment environment) {
        String registryType = environment.getProperty("spring.agg.registry.registry-type");
        if (StringUtils.isBlank(registryType) || RegistryType.direct.name().equals(registryType)) {
            putIntoAggServerProperties("spring.cloud.nacos.discovery.enabled", "false");
            putIntoAggServerProperties("spring.cloud.zookeeper.enabled", "false");
        } else if (RegistryType.zookeeper.name().equals(registryType)) {
            putIntoAggServerProperties("spring.cloud.nacos.discovery.enabled", "false");
            putIntoAggServerProperties("spring.cloud.zookeeper.enabled", "true");

            putIntoAggServerProperties("spring.cloud.zookeeper.connect-string", environment.getProperty("spring.agg.registry.zookeeper.connect-string"));
        } else if (RegistryType.nacos.name().equals(registryType)) {
            putIntoAggServerProperties("spring.cloud.nacos.discovery.enabled", "true");
            putIntoAggServerProperties("spring.cloud.zookeeper.enabled", "false");

            putIntoAggServerProperties("spring.cloud.nacos.discovery.server-addr", environment.getProperty("spring.agg.registry.nacos.server-addr"));
            putIntoAggServerProperties("spring.cloud.nacos.discovery.username", environment.getProperty("spring.agg.registry.nacos.username"));
            putIntoAggServerProperties("spring.cloud.nacos.discovery.password", environment.getProperty("spring.agg.registry.nacos.password"));
        } else {
            logger.warn("unable to reset the registry config of spring clound for {}", registryType);
        }

    }

    private void putIntoAggServerProperties(String key, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        this.aggServerProperties.put(key, value);

    }

}
