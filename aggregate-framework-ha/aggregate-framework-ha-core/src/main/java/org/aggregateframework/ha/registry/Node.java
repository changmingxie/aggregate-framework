package org.aggregateframework.ha.registry;

import java.io.Serializable;

public abstract class Node implements Serializable {

    private static final long serialVersionUID = 5796347772728314654L;

    private int port;
    private String host;
    private String password;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
