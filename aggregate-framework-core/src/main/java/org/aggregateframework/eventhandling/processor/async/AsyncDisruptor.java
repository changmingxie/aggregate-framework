package org.aggregateframework.eventhandling.processor.async;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by changming.xie on 11/24/17.
 */
public class AsyncDisruptor {

    private static final int SLEEP_MILLIS_BETWEEN_DRAIN_ATTEMPTS = 50;
    private static final int MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN = 200;

    private static Map<Class, Disruptor<AsyncEvent>> disruptorMap = new ConcurrentHashMap<Class, Disruptor<AsyncEvent>>();


    public static void ensureStart(Class payloadType) {

        if (!disruptorMap.containsKey(payloadType)) {
            synchronized (payloadType) {
                if (!disruptorMap.containsKey(payloadType)) {

                    int ringBufferSize = 4 * 1024;

                    final ThreadFactory threadFactory = new EventProcessThreadFactory("AsyncDisruptorEventProcessThreadFactory", true, Thread.NORM_PRIORITY) {
                        @Override
                        public Thread newThread(final Runnable r) {
                            final Thread result = super.newThread(r);
                            return result;
                        }
                    };

                    WaitStrategy waitStrategy = new TimeoutBlockingWaitStrategy(10l, TimeUnit.MILLISECONDS);

                    Disruptor<AsyncEvent> disruptor = new Disruptor<AsyncEvent>(AsyncEvent.FACTORY, ringBufferSize, threadFactory, ProducerType.MULTI, waitStrategy);

                    ExceptionHandler<AsyncEvent> errorHandler = new EventProcessDefaultExceptionHandler();

                    disruptor.setDefaultExceptionHandler(errorHandler);

                    final AsyncEventHandler[] asyncEventHandlers = {new AsyncEventHandler()};
                    disruptor.handleEventsWith(asyncEventHandlers);

                    disruptor.start();
                }
            }
        }
    }

    /**
     * Decreases the reference count. If the reference count reached zero, the Disruptor and its associated thread are
     * shut down and their references set to {@code null}.
     */
    public static boolean stop(final long timeout, final TimeUnit timeUnit) {

        Map<Class, Disruptor<AsyncEvent>> tempDisruptorMap = new ConcurrentHashMap<Class, Disruptor<AsyncEvent>>();

        tempDisruptorMap.putAll(disruptorMap);

        if (tempDisruptorMap.isEmpty()) {
            return true; // disruptor was already shut down by another thread
        }

        for (Disruptor<AsyncEvent> disruptor : disruptorMap.values()) {
            // We must guarantee that publishing to the RingBuffer has stopped before we call disruptor.shutdown().
            disruptor = null; // client code fails with NPE if log after stop. This is by design.
        }

        for (Disruptor<AsyncEvent> temp : tempDisruptorMap.values()) {
            // Calling Disruptor.shutdown() will wait until all enqueued events are fully processed,
            // but this waiting happens in a busy-spin. To avoid (postpone) wasting CPU,
            // we sleep in short chunks, up to 10 seconds, waiting for the ringbuffer to drain.
            for (int i = 0; hasBacklog(temp) && i < MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN; i++) {
                try {
                    Thread.sleep(SLEEP_MILLIS_BETWEEN_DRAIN_ATTEMPTS); // give up the CPU for a while
                } catch (final InterruptedException e) { // ignored
                }
            }
            try {
                // busy-spins until all events currently in the disruptor have been processed, or timeout
                temp.shutdown(timeout, timeUnit);
            } catch (final TimeoutException e) {
                temp.halt(); // give up on remaining events, if any
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the specified disruptor still has unprocessed events.
     */
    private static boolean hasBacklog(final Disruptor<?> theDisruptor) {
        final RingBuffer<?> ringBuffer = theDisruptor.getRingBuffer();
        return !ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize());
    }

    public static boolean tryPublish(AsyncEventTranslator eventTranslator) {
        try {
            return disruptorMap.get(eventTranslator.getPayloadType()).getRingBuffer().tryPublishEvent(eventTranslator);
        } catch (final NullPointerException npe) {
            // LOG4J2-639: catch NPE if disruptor field was set to null in stop()
            return false;
        }
    }
}
