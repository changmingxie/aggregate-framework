package org.aggregateframework.eventhandling.processor;

import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.annotation.QueueFullPolicy;
import org.aggregateframework.eventhandling.processor.async.AsyncDisruptor;
import org.aggregateframework.eventhandling.processor.async.AsyncEventTranslator;
import org.aggregateframework.exception.SystemException;
import org.aggregateframework.threadcontext.ThreadContextSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by changmingxie on 12/2/15.
 */
public class AsyncMethodInvoker {

    private static final Logger logger = LoggerFactory.getLogger(AsyncMethodInvoker.class);

    private static volatile AsyncMethodInvoker INSTANCE = null;

    private final ThreadLocal<AsyncEventTranslator> threadLocalTranslator = new ThreadLocal<AsyncEventTranslator>();


    private AsyncMethodInvoker() {

    }

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

        try (AsyncEventTranslator eventTranslator = getCachedTranslator()) {

            eventTranslator.reset(eventInvokerEntry,
                    ThreadContextSynchronizationManager.getThreadContextSynchronization().getCurrentThreadContext());

            if (!AsyncDisruptor.tryPublish(eventTranslator)) {
                logger.info(String.format("agg ring buffer is full, eventHandler will be executed according your QueueFullPolicy, %s.%s",
                        eventInvokerEntry.getTarget().getClass().getSimpleName(),
                        eventInvokerEntry.getMethod().getName()));
                handleRingBufferFull(eventTranslator);
            }

        } catch (Exception e) {
            throw new SystemException(e);
        }

        return;
    }

    private void handleRingBufferFull(AsyncEventTranslator eventTranslator) {

        EventInvokerEntry eventInvokerEntry = eventTranslator.getEventInvokerEntry();

        EventHandler eventHandler = eventInvokerEntry.getMethod().getAnnotation(EventHandler.class);

        final QueueFullPolicy queueFullPolicy = eventHandler.asyncConfig().queueFullPolicy();

        switch (queueFullPolicy) {
            case DISCARD:
                break;
            case ENQUEUE:
                AsyncDisruptor.publish(eventTranslator);
                break;
            case SYNCHRONOUS:
                EventMethodInvoker.getInstance().invoke(eventInvokerEntry);
                break;
            default:
                throw new IllegalStateException("Unknown QueueFullPolicy " + queueFullPolicy);
        }
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
}
