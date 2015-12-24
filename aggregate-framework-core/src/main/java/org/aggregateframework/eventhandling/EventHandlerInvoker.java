package org.aggregateframework.eventhandling;

import org.aggregateframework.SystemException;
import org.aggregateframework.context.ReflectionUtils;
import org.aggregateframework.eventhandling.async.AsyncMethodInvoker;
import org.aggregateframework.session.EventInvokerEntry;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by changmingxie on 12/2/15.
 */
public class EventHandlerInvoker {
    public static void invoke(EventInvokerEntry eventInvokerEntry) {

        EventHandler eventHandler = ReflectionUtils.getAnnotation(eventInvokerEntry.getMethod(), EventHandler.class);
        if (eventHandler.asynchronous()) {
            AsyncMethodInvoker.getInstance().invoke(eventInvokerEntry.getMethod(), eventInvokerEntry.getTarget(), eventInvokerEntry.getParams());
        } else {
            try {
                eventInvokerEntry.getMethod().invoke(eventInvokerEntry.getTarget(), eventInvokerEntry.getParams());
            } catch (IllegalAccessException e) {
                throw new SystemException(e);
            } catch (InvocationTargetException e) {
                throw new SystemException(e);
            }
        }
    }
}
