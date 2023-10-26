package org.aggregateframework.discovery.registry;

/**
 * @author Nervose.Wu
 * @date 2022/7/7 17:34
 */
public interface ServerRegistryConfig extends RegistryConfig {

    String getRegistryAddress();

    int getRegistryPortForDashboard();

    String getRegistryAddressForDashboard();

}
