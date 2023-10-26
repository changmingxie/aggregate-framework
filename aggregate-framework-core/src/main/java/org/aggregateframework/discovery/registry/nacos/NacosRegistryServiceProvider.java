package org.aggregateframework.discovery.registry.nacos;

import org.aggregateframework.discovery.registry.RegistryConfig;
import org.aggregateframework.discovery.registry.RegistryProvider;
import org.aggregateframework.discovery.registry.RegistryService;
import org.aggregateframework.load.LoadInfo;

/**
 * @author Nervose.Wu
 * @date 2022/5/12 17:29
 */
@LoadInfo(name = "nacos")
public class NacosRegistryServiceProvider implements RegistryProvider {
    @Override
    public RegistryService provide(RegistryConfig registryConfig) {
        return new NacosRegistryServiceImpl(registryConfig);
    }
}
