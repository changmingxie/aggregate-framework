package org.aggregateframework.eventhandling.async;

import com.lmax.disruptor.EventFactory;
import org.aggregateframework.eventhandling.async.AsyncEvent;

/**
 * Created by changmingxie on 12/2/15.
 */
public class AsyncEventFactory implements EventFactory<AsyncEvent> {
    @Override
    public AsyncEvent newInstance() {
        return new AsyncEvent();
    }
}
