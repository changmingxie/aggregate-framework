package org.aggregateframework.discovery.loadbalance;

import org.aggregateframework.discovery.registry.ClientRegistryConfig;
import org.aggregateframework.load.LoadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Nervose.Wu
 * @date 2022/5/12 17:18
 */
public class LoadBalanceFactory {

    private static final Map<String, LoadBalanceProvider> CANDIDATE_LOAD_BALANCES = new HashMap<>();

    static {
        ServiceLoader.load(LoadBalanceProvider.class)
                .forEach(each -> CANDIDATE_LOAD_BALANCES.put(LoadUtils.getServiceName(each.getClass()), each));
    }

    private LoadBalanceFactory() {
    }

    public static LoadBalanceServcie getInstance(ClientRegistryConfig clientRegistryConfig) {
        String loadBalanceType = clientRegistryConfig.getLoadBalanceType();
        if (!CANDIDATE_LOAD_BALANCES.containsKey(loadBalanceType)) {
            throw new IllegalArgumentException("invalid loadBalanceType: " + loadBalanceType);
        }
        return CANDIDATE_LOAD_BALANCES.get(loadBalanceType).provide();

    }
}
