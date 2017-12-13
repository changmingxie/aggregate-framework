package org.aggregateframework.sample.asynctest;

import com.lmax.disruptor.EventFactory;

/**
 * Created by changming.xie on 11/23/17.
 */
public class RingBufferEvent {

    public static final Factory FACTORY = new Factory();

    int i;

    public void execute(boolean endOfBatch) {

        System.out.println("async call, i:" + i);

        if (i == 100) {
            try {
                Thread.sleep(100 * 1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        i = 0;
    }

    public void clear() {

    }

    public void set(int i) {
        this.i = i;
    }

    private static class Factory implements EventFactory<RingBufferEvent> {

        @Override
        public RingBufferEvent newInstance() {
            final RingBufferEvent result = new RingBufferEvent();
            return result;
        }
    }
}