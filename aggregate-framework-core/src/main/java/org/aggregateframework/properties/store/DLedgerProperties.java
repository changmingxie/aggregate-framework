package org.aggregateframework.properties.store;

import com.alibaba.fastjson.JSON;
import io.openmessaging.storage.dledger.DLedgerConfig;
import io.openmessaging.storage.dledger.proxy.DLedgerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DLedgerProperties {

    private static Logger logger = LoggerFactory.getLogger(DLedgerProperties.class);

    private List<DLedgerConfig> configs=new ArrayList<>();

    public List<DLedgerConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<DLedgerConfig> configs) {
        this.configs = configs;
    }


    public DLedgerProxy getDLedgerProxy() {
        List<DLedgerConfig> dLedgerConfigs = buildDLedgerConfigs();
        DLedgerProxy dLedgerProxy = new DLedgerProxy(dLedgerConfigs);
        dLedgerProxy.startup();
        logger.info("DLedgers start ok with config {}", JSON.toJSONString(dLedgerConfigs));
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            private volatile boolean hasShutdown = false;

            @Override
            public void run() {
                synchronized (this) {
                    logger.info("Shutdown hook was invoked");
                    if (!this.hasShutdown) {
                        this.hasShutdown = true;
                        long beginTime = System.currentTimeMillis();
                        dLedgerProxy.shutdown();
                        long consumingTimeTotal = System.currentTimeMillis() - beginTime;
                        logger.info("Shutdown hook over, consuming total time(ms): {}", consumingTimeTotal);
                    }
                }
            }
        }, "ShutdownHook"));
        return dLedgerProxy;
    }

    private List<DLedgerConfig> buildDLedgerConfigs() {
        List<DLedgerConfig> dLedgerConfigs = new ArrayList<>();

        if(this.configs == null) {
            dLedgerConfigs.add(new DLedgerConfig());
        } else {
            dLedgerConfigs.addAll(this.configs);
        }

        return dLedgerConfigs;
    }
}
