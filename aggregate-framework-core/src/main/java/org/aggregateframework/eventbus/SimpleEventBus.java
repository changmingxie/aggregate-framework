package org.aggregateframework.eventbus;

import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.EventListener;
import org.aggregateframework.eventhandling.SimpleEventListenerProxy;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.processor.EventHandlerProcessor;
import org.aggregateframework.session.LocalSessionFactory;
import org.aggregateframework.transaction.LocalTransactionExecutor;
import org.aggregateframework.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * User: changming.xie
 * Date: 14-7-3
 * Time: 下午8:16
 */
public class SimpleEventBus implements EventBus {

    public static final EventBus INSTANCE = new SimpleEventBus();

    private final Set<EventListener> listeners = new CopyOnWriteArraySet<EventListener>();

    @Override
    public void publishInTransaction(EventMessage[] messages, LocalTransactionExecutor localTransactionExecutor) {

        List<EventInvokerEntry> eventInvokerEntries = new ArrayList<EventInvokerEntry>();

        if (!listeners.isEmpty()) {
            for (EventMessage event : messages) {
                for (EventListener listener : listeners) {
                    eventInvokerEntries.addAll(listener.matchHandler(event));
                }
            }
        }

        for (EventInvokerEntry eventInvokerEntry : eventInvokerEntries) {
            preHandle(eventInvokerEntry);
        }

        localTransactionExecutor.executeLocalTransactionBranch(messages);

        for (EventInvokerEntry eventInvokerEntry : eventInvokerEntries) {
            handle(eventInvokerEntry);
        }
    }

    @Override
    public void subscribe(EventListener eventListener) {

        if (eventListener instanceof SimpleEventListenerProxy) {
            for (EventListener listener : listeners) {
                if (listener instanceof SimpleEventListenerProxy) {
                    SimpleEventListenerProxy thisListener = (SimpleEventListenerProxy) listener;
                    SimpleEventListenerProxy thatListener = (SimpleEventListenerProxy) eventListener;
                    if (thisListener.getTargetType().equals(thatListener.getTargetType())) {
                        return;
                    }
                }
            }
        }

        listeners.add(eventListener);
    }

    private void preHandle(EventInvokerEntry eventInvokerEntry) {
        EventHandler eventHandler = ReflectionUtils.getAnnotation(eventInvokerEntry.getMethod(), EventHandler.class);

        if (eventHandler.isTransactionMessage()) {
            EventHandlerProcessor.prepare(eventInvokerEntry);
        }
    }

    private void handle(EventInvokerEntry eventInvokerEntry) {
        EventHandler eventHandler = ReflectionUtils.getAnnotation(eventInvokerEntry.getMethod(), EventHandler.class);

        if (eventHandler.postAfterTransaction()) {
            LocalSessionFactory.INSTANCE.requireClientSession().addPostInvoker(eventInvokerEntry);
        } else {
            EventHandlerProcessor.proceed(eventInvokerEntry);
        }
    }


}
