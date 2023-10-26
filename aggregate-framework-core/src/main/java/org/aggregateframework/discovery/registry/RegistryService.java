package org.aggregateframework.discovery.registry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Nervose.Wu
 * @date 2022/5/12 16:27
 */
public interface RegistryService {

    void start();

    void register(InetSocketAddress addressForClient, InetSocketAddress addressForDashboard);

    void subscribe(boolean forDashboard);

    List<String> lookup(boolean forDashboard);

    void close();
}
