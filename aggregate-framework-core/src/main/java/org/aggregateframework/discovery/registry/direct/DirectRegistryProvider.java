package org.aggregateframework.discovery.registry.direct;

import org.aggregateframework.discovery.registry.RegistryConfig;
import org.aggregateframework.discovery.registry.RegistryProvider;
import org.aggregateframework.discovery.registry.RegistryService;
import org.aggregateframework.load.LoadInfo;

/**
 * @author Nervose.Wu
 * @date 2022/5/18 17:11
 */
@LoadInfo(name = "direct")
public class DirectRegistryProvider implements RegistryProvider {
    @Override
    public RegistryService provide(RegistryConfig registryConfig) {
        return new DirectRegistryServiceImpl(registryConfig);
    }
}
