package org.aggregateframework.ignite.store;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by changming.xie on 10/28/16.
 */
public class IgniteCacheStarter implements ApplicationListener<ContextRefreshedEvent>, InitializingBean {

    private String configFile = "config/ignite/ignite-cache.xml";

    private IgniteCacheLoader cacheLoader;

    private String gridName;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            start();
        }
    }

    private void start() {


        if (cacheLoader != null) {
            cacheLoader.load(Ignition.ignite(gridName));
        }
    }

    public void setCacheLoader(IgniteCacheLoader cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (!Ignition.state(gridName).equals(IgniteState.STARTED)) {

            if (StringUtils.isNotEmpty(configFile)) {
                Ignition.start(configFile);
            } else {
                Ignition.start();
            }
        }
    }

    public void setGridName(String gridName) {
        this.gridName = gridName;
    }
}
