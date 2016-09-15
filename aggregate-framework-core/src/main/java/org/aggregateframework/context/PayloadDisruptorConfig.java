package org.aggregateframework.context;

import java.util.concurrent.Executor;

/**
 * Created by changming.xie on 9/13/16.
 */
public class PayloadDisruptorConfig {

    private Class payloadType;

    private int ringBufferSize = 2048;

    private int workPoolSize = 10;

    Executor executor;

    public PayloadDisruptorConfig(Class payloadType) {
        this.payloadType = payloadType;
        executor = AsyncParameterConfig.DEFAULT_EXECUTOR;
    }

    public PayloadDisruptorConfig(Class payloadType, int ringBufferSize, int workPoolSize) {
        this.payloadType = payloadType;
        this.ringBufferSize = ringBufferSize;
        this.workPoolSize = workPoolSize;
        executor = AsyncParameterConfig.DEFAULT_EXECUTOR;
    }

    public Class getPayloadType() {
        return payloadType;
    }

    public int getRingBufferSize() {
        return ringBufferSize;
    }

    public int getWorkPoolSize() {
        return workPoolSize;
    }

    public Executor getExecutor() {
        return this.executor;
    }
}
