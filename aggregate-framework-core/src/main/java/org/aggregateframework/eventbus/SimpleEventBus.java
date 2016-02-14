package org.aggregateframework.eventbus;

import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.eventhandling.EventListener;
import org.aggregateframework.eventhandling.SimpleEventListenerProxy;

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
    public void publish(EventMessage[] events) {
        if (!listeners.isEmpty()) {
            for (EventMessage event : events) {
                for (EventListener listener : listeners) {
                    listener.handle(event);
                }
            }
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
}
