package org.aggregateframework.ha.registry.jedis;

import org.aggregateframework.ha.registry.Registration;

import java.util.HashSet;
import java.util.Set;

/**
 * jedis 集群 注册信息
 */
public class JedisClusterRegistration extends Registration {
    
    
    private Set<JedisNode> nodes = new HashSet<>();


    public Set<JedisNode> getNodes() {
        return nodes;
    }

    public void setNodes(Set<JedisNode> nodes) {
        this.nodes = nodes;
    }
}
