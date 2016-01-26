package org.aggregateframework.spring.context;

import org.aggregateframework.context.AsyncParameterConfig;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.Executor;

/**
 * Created by hongyuan.wang on 2016/1/21.
 */
public class DisruptorParameterConfig implements InitializingBean {

    private int disruptorRingBufferSize;

    private Executor executor;

    private int asyncEventHandlerWorkpoolSize;

    public int getDisruptorRingBufferSize() {
        return disruptorRingBufferSize;
    }

    public void setDisruptorRingBufferSize(int disruptorRingBufferSize) {
        this.disruptorRingBufferSize = disruptorRingBufferSize;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public int getAsyncEventHandlerWorkpoolSize() {
        return asyncEventHandlerWorkpoolSize;
    }

    public void setAsyncEventHandlerWorkpoolSize(int asyncEventHandlerWorkpoolSize) {
        this.asyncEventHandlerWorkpoolSize = asyncEventHandlerWorkpoolSize;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (disruptorRingBufferSize > 0) {
            AsyncParameterConfig.DISRUPTOR_RING_BUFFER_SIZE = this.disruptorRingBufferSize;
        }

        if (asyncEventHandlerWorkpoolSize > 0) {
            AsyncParameterConfig.ASYNC_EVENT_HANDLER_WORKPOOL_SIZE = this.asyncEventHandlerWorkpoolSize;
        }

        if (executor != null) {
            AsyncParameterConfig.EXECUTOR = executor;
        }
    }
}
