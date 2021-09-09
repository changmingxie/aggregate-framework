package org.aggregateframework.ha.registry.jedis;

import org.aggregateframework.ha.registry.Registration;

/**
 * Created by Lee on 2020/9/12 10:48.
 * aggregate-framework
 */
public class JedisRegistration extends Registration {
    
    private JedisNode node;

    public JedisNode getNode() {
        return node;
    }

    public void setNode(JedisNode node) {
        this.node = node;
    }
}
