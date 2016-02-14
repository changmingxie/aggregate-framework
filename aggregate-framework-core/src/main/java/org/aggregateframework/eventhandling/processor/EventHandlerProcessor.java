package org.aggregateframework.eventhandling.processor;

import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.utils.ReflectionUtils;

/**
 * Created by changmingxie on 12/2/15.
 */
public class EventHandlerProcessor {
    public static void proceed(EventInvokerEntry eventInvokerEntry) {

        EventHandler eventHandler = ReflectionUtils.getAnnotation(eventInvokerEntry.getMethod(), EventHandler.class);
        if (eventHandler.asynchronous()) {
            AsyncMethodInvoker.getInstance().invoke(eventInvokerEntry.getMethod(), eventInvokerEntry.getTarget(), eventInvokerEntry.getParams());
        } else {
            SyncMethodInvoker.getInstance().invoke(eventInvokerEntry.getMethod(), eventInvokerEntry.getTarget(), eventInvokerEntry.getParams());
        }
    }
}
