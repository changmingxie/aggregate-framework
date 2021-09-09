package org.aggregateframework.ha.registry.jdbc;

import org.aggregateframework.ha.registry.Node;

/**
 * Created by Lee on 2020/9/14 14:02.
 * aggregate-framework
 */
public class JDBCNode extends Node {

    private static final long serialVersionUID = 7303789787631414223L;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String username;
    private String url;
    
    
}
