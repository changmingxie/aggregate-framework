package org.aggregateframework.test.discruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by changmingxie on 11/29/15.
 */
public class LongEventMain {

    public static void main(String[] args) throws InterruptedException {

        Executor executor = Executors.newSingleThreadExecutor();

        LongEventFactory factory = new LongEventFactory();

        int bufferSize = 1024;

        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(factory, bufferSize, executor);

        disruptor.handleEventsWith(new LongEventHandler());

        disruptor.start();

        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducer producer = new LongEventProducer(ringBuffer);

        ByteBuffer byteBuffer = ByteBuffer.allocate(8);

        for (long i = 0; true; i++) {
            byteBuffer.putLong(0, i);
            producer.onData(byteBuffer);

//            Thread.sleep(500);
        }

    }
}
