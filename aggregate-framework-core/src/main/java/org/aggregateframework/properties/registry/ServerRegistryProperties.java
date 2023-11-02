package org.aggregateframework.properties.registry;


import org.aggregateframework.discovery.registry.ServerRegistryConfig;

/**
 * @author Nervose.Wu
 * @date 2022/7/7 17:38
 */
public class ServerRegistryProperties extends RegistryProperties implements ServerRegistryConfig {

    private String registryAddress;

    private int registryPortForDashboard = 12332;

    private String registryAddressForDashboard;

    @Override
    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    @Override
    public int getRegistryPortForDashboard() {
        return registryPortForDashboard;
    }

    public void setRegistryPortForDashboard(int registryPortForDashboard) {
        this.registryPortForDashboard = registryPortForDashboard;
    }

    @Override
    public String getRegistryAddressForDashboard() {
        return registryAddressForDashboard;
    }

    public void setRegistryAddressForDashboard(String registryAddressForDashboard) {
        this.registryAddressForDashboard = registryAddressForDashboard;
    }
}
