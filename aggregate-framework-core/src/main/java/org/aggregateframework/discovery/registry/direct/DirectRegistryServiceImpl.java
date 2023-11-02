package org.aggregateframework.discovery.registry.direct;

import org.aggregateframework.discovery.registry.AbstractRegistryService;
import org.aggregateframework.discovery.registry.RegistryConfig;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Nervose.Wu
 * @date 2022/5/18 17:11
 */
public class DirectRegistryServiceImpl extends AbstractRegistryService {

    private DirectRegistryProperties properties;

    public DirectRegistryServiceImpl(RegistryConfig registryConfig) {
        this.properties = registryConfig.getDirectRegistryProperties();
    }

    @Override
    protected void doRegister(InetSocketAddress addressForClient, InetSocketAddress addressForDashboard) throws Exception {
        //do nothing
    }

    @Override
    protected void doSubscribe(boolean forDashboard) throws Exception {
        String addresses = forDashboard ? properties.getAddressesForDashboard() : properties.getAddressesForClient();
        if (StringUtils.isBlank(addresses)) {
            throw new IllegalArgumentException("AddressesForClient/AddressesForDashboard cant be blank");
        }
        try {
            setServerAddresses(Arrays
                    .stream(addresses.split(","))
                    .collect(Collectors.toList()), forDashboard);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse addressesForClient/addressesForDashboard:" + addresses);
        }
    }
}
