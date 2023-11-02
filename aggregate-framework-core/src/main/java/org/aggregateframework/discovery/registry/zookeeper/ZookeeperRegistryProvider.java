package org.aggregateframework.discovery.registry.zookeeper;

import org.aggregateframework.discovery.registry.RegistryConfig;
import org.aggregateframework.discovery.registry.RegistryProvider;
import org.aggregateframework.discovery.registry.RegistryService;
import org.aggregateframework.load.LoadInfo;

/**
 * @author Nervose.Wu
 * @date 2022/5/12 17:27
 */
@LoadInfo(name = "zookeeper")
public class ZookeeperRegistryProvider implements RegistryProvider {
    @Override
    public RegistryService provide(RegistryConfig registryConfig) {
        return new ZookeeperRegistryServiceImpl(registryConfig);
    }
}
