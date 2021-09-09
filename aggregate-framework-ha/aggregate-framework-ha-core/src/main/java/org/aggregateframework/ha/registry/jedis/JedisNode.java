package org.aggregateframework.ha.registry.jedis;

import org.aggregateframework.ha.registry.Node;

/**
 * Created by Lee on 2020/9/14 14:01.
 * aggregate-framework
 */
public class JedisNode extends Node {

    private static final long serialVersionUID = 7727809129459627921L;
    private int database;

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

}
