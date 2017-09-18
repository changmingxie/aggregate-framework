package org.aggregateframework.ignite.store;

import org.apache.ignite.Ignite;

/**
 * Created by changming.xie on 10/28/16.
 */
public interface IgniteCacheLoader {

    void load(Ignite ignite);
}
