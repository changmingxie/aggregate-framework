package org.aggregateframework.discovery.loadbalance;

import org.aggregateframework.load.LoadInfo;

/**
 * @author Nervose.Wu
 * @date 2022/5/19 14:50
 */
@LoadInfo(name = "RoundRobin")
public class RoundRobinLoadBalanceProvider implements LoadBalanceProvider {
    @Override
    public LoadBalanceServcie provide() {
        return new RoundRobinLoadBalanceServcieImpl();
    }
}
