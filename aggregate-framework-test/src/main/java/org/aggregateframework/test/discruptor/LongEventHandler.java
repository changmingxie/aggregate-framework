package org.aggregateframework.test.discruptor;

import com.lmax.disruptor.EventHandler;

/**
 * Created by changmingxie on 11/29/15.
 */
public class LongEventHandler implements EventHandler<LongEvent> {

    @Override
    public void onEvent(LongEvent longEvent, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("thread id:"+Thread.currentThread().getId()+"thread:"+Thread.currentThread()+",event:"+longEvent.getValue()+", sequence:"+sequence+",endOfBatch:"+endOfBatch);
    }
}
