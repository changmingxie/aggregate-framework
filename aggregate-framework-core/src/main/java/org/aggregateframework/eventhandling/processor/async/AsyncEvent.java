package org.aggregateframework.eventhandling.processor.async;

import org.aggregateframework.eventhandling.EventInvokerEntry;

/**
 * Created by changmingxie on 12/2/15.
 */
public class AsyncEvent {

    public static final AsyncEventFactory FACTORY = new AsyncEventFactory();

    private EventInvokerEntry eventInvokerEntry;

    public void reset(EventInvokerEntry eventInvokerEntry) {
        this.eventInvokerEntry = eventInvokerEntry;
    }

    public EventInvokerEntry getEventInvokerEntry() {
        return eventInvokerEntry;
    }

    public void clear() {
        eventInvokerEntry = null;
    }
}
