package org.aggregateframework.test.discruptor;

import com.lmax.disruptor.EventFactory;

/**
 * Created by changmingxie on 11/29/15.
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}
