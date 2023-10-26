package org.aggregateframework.eventhandling.processor.async;

import com.lmax.disruptor.EventTranslator;
import org.aggregateframework.eventhandling.EventInvokerEntry;

/**
 * Created by changming.xie on 11/24/17.
 */
public class AsyncEventTranslator implements EventTranslator<AsyncEvent>, AutoCloseable {

    private EventInvokerEntry eventInvokerEntry;
    private String threadContext;

    @Override
    public void translateTo(AsyncEvent event, long sequence) {
        event.reset(eventInvokerEntry, threadContext);
    }

    public void reset(EventInvokerEntry eventInvokerEntry, String threadContext) {
        this.eventInvokerEntry = eventInvokerEntry;
        this.threadContext = threadContext;
    }

    public EventInvokerEntry getEventInvokerEntry() {
        return eventInvokerEntry;
    }

    @Override
    public void close() throws Exception {
        this.eventInvokerEntry = null;
        this.threadContext = null;
    }
}
