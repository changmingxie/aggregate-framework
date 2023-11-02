package org.aggregateframework.properties.registry;

import org.aggregateframework.discovery.loadbalance.LoadBalanceType;
import org.aggregateframework.discovery.registry.ClientRegistryConfig;
import org.aggregateframework.discovery.registry.RegistryRole;

/**
 * @author Nervose.Wu
 * @date 2022/7/7 17:38
 */
public class ClientRegistryProperties extends RegistryProperties implements ClientRegistryConfig {

    private String loadBalanceType = LoadBalanceType.RoundRobin.name();

    private RegistryRole registryRole = RegistryRole.CLIENT;

    @Override
    public RegistryRole getRegistryRole() {
        return registryRole;
    }

    public void setRegistryRole(RegistryRole registryRole) {
        this.registryRole = registryRole;
    }

    @Override
    public String getLoadBalanceType() {
        return loadBalanceType;
    }

    public void setLoadBalanceType(String loadBalanceType) {
        this.loadBalanceType = loadBalanceType;
    }
}
