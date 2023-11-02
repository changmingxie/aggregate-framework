package org.aggregateframework.discovery.registry;

import org.aggregateframework.exception.RegistryException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Nervose.Wu
 * @date 2022/5/17 11:09
 */
public abstract class AbstractRegistryService implements RegistryService {

    private String clusterName;

    private volatile List<String> addressesForClient = new ArrayList<>();

    private volatile List<String> addressesForDashboard = new ArrayList<>();

    @Override
    public void start() {
        //do nothing by default
    }

    @Override
    public void register(InetSocketAddress addressForClient, InetSocketAddress addressForDashboard) {
        try {
            doRegister(addressForClient, addressForDashboard);
        } catch (Exception e) {
            throw new RegistryException(e);
        }
    }

    @Override
    public void subscribe(boolean forDashboard) {
        try {
            doSubscribe(forDashboard);
        } catch (Exception e) {
            throw new RegistryException(e);
        }
    }

    @Override
    public List<String> lookup(boolean forDashboard) {
        return forDashboard ? addressesForDashboard : addressesForClient;
    }

    @Override
    public void close() {
        //do nothing by default
    }

    protected abstract void doRegister(InetSocketAddress addressForClient, InetSocketAddress addressForDashboard) throws Exception;

    protected abstract void doSubscribe(boolean forDashboard) throws Exception;

    protected void setServerAddresses(List<String> addresses, boolean forDashboard) {
        Collections.shuffle(addresses, ThreadLocalRandom.current());
        if (forDashboard) {
            this.addressesForDashboard = addresses;
        } else {
            this.addressesForClient = addresses;
        }
    }

    protected String getClusterName() {
        return clusterName;
    }

    protected void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
