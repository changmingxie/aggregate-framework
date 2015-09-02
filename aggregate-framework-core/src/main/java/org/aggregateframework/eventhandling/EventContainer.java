package org.aggregateframework.eventhandling;


import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: changming.xie
 * Date: 14-7-2
 * Time: 下午7:33
 */
public class EventContainer implements Serializable {

    private final Map<Object, EventMessage> eventMessageMap = new HashMap<Object, EventMessage>();

    public <T> void addEvent(T payload) {

        if (!eventMessageMap.containsKey(payload)) {
            EventMessage eventMessage = new SimpleEventMessage<T>(payload);
            eventMessageMap.put(payload, eventMessage);
        }
    }

    public Set<EventMessage> getEvents() {
        return new HashSet<EventMessage>(eventMessageMap.values());
    }

    public void commit() {
        eventMessageMap.clear();
    }

    public void clear() {
        eventMessageMap.clear();
    }
}
