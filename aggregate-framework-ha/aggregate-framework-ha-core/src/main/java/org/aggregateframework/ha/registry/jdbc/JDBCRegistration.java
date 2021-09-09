package org.aggregateframework.ha.registry.jdbc;

import org.aggregateframework.ha.registry.Registration;

/**
 * Created by Lee on 2020/9/12 10:49.
 * aggregate-framework
 */
public class JDBCRegistration extends Registration {

    private JDBCNode node;

    public JDBCNode getNode() {
        return node;
    }

    public void setNode(JDBCNode node) {
        this.node = node;
    }

}
