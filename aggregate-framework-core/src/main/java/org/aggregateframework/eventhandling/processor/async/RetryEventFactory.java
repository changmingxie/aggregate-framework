package org.aggregateframework.eventhandling.processor.async;

import com.lmax.disruptor.EventFactory;

/**
 * Created by changming.xie on 2/1/16.
 */
public class RetryEventFactory implements EventFactory<RetryEvent> {
    @Override
    public RetryEvent newInstance() {
        return new RetryEvent();
    }
}
