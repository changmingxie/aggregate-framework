package org.aggregateframework.eventhandling.processor.async;

import com.lmax.disruptor.EventTranslator;
import org.aggregateframework.eventhandling.EventInvokerEntry;

/**
 * Created by changming.xie on 11/24/17.
 */
public class AsyncEventTranslator implements EventTranslator<AsyncEvent> {

    private EventInvokerEntry eventInvokerEntry;

    @Override
    public void translateTo(AsyncEvent event, long sequence) {
        event.reset(eventInvokerEntry);
    }

    public void reset(EventInvokerEntry eventInvokerEntry) {
        this.eventInvokerEntry = eventInvokerEntry;
    }

    public EventInvokerEntry getEventInvokerEntry() {
        return eventInvokerEntry;
    }
}
