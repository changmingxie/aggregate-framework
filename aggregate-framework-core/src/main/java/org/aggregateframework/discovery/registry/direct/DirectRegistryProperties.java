package org.aggregateframework.discovery.registry.direct;

/**
 * @author Nervose.Wu
 * @date 2022/5/18 17:00
 */
public class DirectRegistryProperties {

    private String addressesForClient = "127.0.0.1:2332";

    private String addressesForDashboard = "127.0.0.1:12332";

    public String getAddressesForClient() {
        return addressesForClient;
    }

    public void setAddressesForClient(String addressesForClient) {
        this.addressesForClient = addressesForClient;
    }

    public String getAddressesForDashboard() {
        return addressesForDashboard;
    }

    public void setAddressesForDashboard(String addressesForDashboard) {
        this.addressesForDashboard = addressesForDashboard;
    }
}
