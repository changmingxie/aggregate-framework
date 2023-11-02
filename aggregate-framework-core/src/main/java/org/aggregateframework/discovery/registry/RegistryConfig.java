package org.aggregateframework.discovery.registry;

import org.aggregateframework.discovery.registry.nacos.NacosRegistryProperties;
import org.aggregateframework.discovery.registry.direct.DirectRegistryProperties;
import org.aggregateframework.discovery.registry.zookeeper.ZookeeperRegistryProperties;

/**
 * @author Nervose.Wu
 * @date 2022/5/12 18:03
 */
public interface RegistryConfig {

    String getClusterName();

    ZookeeperRegistryProperties getZookeeperRegistryProperties();

    NacosRegistryProperties getNacosRegistryProperties();

    DirectRegistryProperties getDirectRegistryProperties();

    RegistryType getRegistryType();

    String getCustomRegistryName();
}
