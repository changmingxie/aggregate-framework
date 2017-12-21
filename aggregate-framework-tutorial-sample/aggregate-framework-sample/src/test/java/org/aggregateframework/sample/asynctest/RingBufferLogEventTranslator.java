package org.aggregateframework.sample.asynctest;

import com.lmax.disruptor.EventTranslator;

/**
 * Created by changming.xie on 11/23/17.
 */
public class RingBufferLogEventTranslator implements EventTranslator<RingBufferEvent> {
    int i;
    @Override
    public void translateTo(RingBufferEvent event, long sequence) {
        event.set(i);
    }

    public void setIndex(int i) {
     this.i = i;
    }
}
