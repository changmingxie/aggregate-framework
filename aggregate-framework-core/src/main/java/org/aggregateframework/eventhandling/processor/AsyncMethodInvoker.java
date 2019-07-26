package org.aggregateframework.eventhandling.processor;

import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.processor.async.AsyncDisruptor;
import org.aggregateframework.eventhandling.processor.async.AsyncEventTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by changmingxie on 12/2/15.
 */
public class AsyncMethodInvoker {

    static final Logger logger = LoggerFactory.getLogger(AsyncMethodInvoker.class);

    private static volatile AsyncMethodInvoker INSTANCE = null;

    private final ThreadLocal<AsyncEventTranslator> threadLocalTranslator = new ThreadLocal<AsyncEventTranslator>();


    public static AsyncMethodInvoker getInstance() {

        if (INSTANCE == null) {

            synchronized (AsyncMethodInvoker.class) {

                if (INSTANCE == null) {
                    INSTANCE = new AsyncMethodInvoker();
                }
            }
        }

        return INSTANCE;
    }

    public void invoke(EventInvokerEntry eventInvokerEntry) {

        AsyncDisruptor.ensureStart(eventInvokerEntry.getMethod());

        AsyncEventTranslator eventTranslator = getCachedTranslator();

        eventTranslator.reset(eventInvokerEntry);

        if (!AsyncDisruptor.tryPublish(eventTranslator)) {
            logger.warn("agg ring buffer is full, eventHandler will be execute in sync mode, {}.{}",
                    eventInvokerEntry.getTarget().getClass().getSimpleName(),
                    eventInvokerEntry.getMethod().getName());
            handleRingBufferFull(eventInvokerEntry);
        }

        return;
    }


    private void handleRingBufferFull(EventInvokerEntry eventInvokerEntry) {
        SyncMethodInvoker.getInstance().invoke(eventInvokerEntry);
    }

    private AsyncEventTranslator getCachedTranslator() {
        AsyncEventTranslator result = threadLocalTranslator.get();
        if (result == null) {
            result = new AsyncEventTranslator();
            threadLocalTranslator.set(result);
        }
        return result;
    }


    public void shutdown() {
        AsyncDisruptor.stop(60, TimeUnit.SECONDS);
    }

    private AsyncMethodInvoker() {

    }
}
