package org.aggregateframework.spring.support;

import org.aggregateframework.context.AsyncParameterConfig;
import org.aggregateframework.context.PayloadDisruptorConfig;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * Created by hongyuan.wang on 2016/1/21.
 */
public class DisruptorParameterConfig implements InitializingBean {

    private List<PayloadDisruptorConfig> payloadDisruptorConfigs;

    @Override
    public void afterPropertiesSet() throws Exception {
        AsyncParameterConfig.PAYLOAD_TYPE_DISRUPTOR_CONFIGS.addAll(payloadDisruptorConfigs);
    }

    public void setPayloadDisruptorConfigs(List<PayloadDisruptorConfig> payloadDisruptorConfigs) {
        this.payloadDisruptorConfigs = payloadDisruptorConfigs;
    }
}
