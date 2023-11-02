package org.aggregateframework.entity;


import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.domainevent.SimpleEventMessage;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: changming.xie
 * Date: 14-7-2
 * Time: 下午7:33
 */
public class EventContainer implements Serializable {

    private final Map<Object, EventMessage> eventMessageMap = new LinkedHashMap<Object, EventMessage>();

    public <T> void addEvent(T payload) {

        if (!eventMessageMap.containsKey(payload)) {
            EventMessage eventMessage = new SimpleEventMessage<T>(payload);
            eventMessageMap.put(payload, eventMessage);
        }
    }

    public Collection<EventMessage> getEvents() {
        return eventMessageMap.values();
    }

    public void commit() {
        eventMessageMap.clear();
    }

    public void clear() {
        eventMessageMap.clear();
    }
}
