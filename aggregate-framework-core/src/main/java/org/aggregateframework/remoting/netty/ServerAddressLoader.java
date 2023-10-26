package org.aggregateframework.remoting.netty;

import java.util.List;

public interface ServerAddressLoader {
    String selectOneAvailableAddress();

    List<String> getAllAvailableAddresses();

    boolean isAvailableAddress(String address);
}
