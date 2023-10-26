package org.aggregateframework.eventhandling.processor.async;

import com.lmax.disruptor.EventHandler;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.processor.EventMethodInvoker;
import org.aggregateframework.threadcontext.ThreadContextSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsyncBatchEventHandler implements EventHandler<AsyncEvent> {

    static final Logger logger = LoggerFactory.getLogger(AsyncBatchEventHandler.class);

    private List<AsyncEvent> asyncEvents = new ArrayList<AsyncEvent>();


    @Override
    public void onEvent(AsyncEvent event, long sequence, boolean endOfBatch) throws Exception {

        asyncEvents.add(event);

        org.aggregateframework.eventhandling.annotation.EventHandler eventHandler = event.getEventInvokerEntry().getMethod().getAnnotation(org.aggregateframework.eventhandling.annotation.EventHandler.class);

        int maxBatchSize = eventHandler.asyncConfig().maxBatchSize();

        if (asyncEvents.size() >= maxBatchSize || endOfBatch) {

            logger.debug("batch done, seq:" + sequence + ", total events:" + asyncEvents.size());


            Map<String, List<AsyncEvent>> threadContextGroupedEventsMap = new LinkedHashMap<>();

            for (AsyncEvent asyncEvent : asyncEvents) {

                if (!threadContextGroupedEventsMap.containsKey(asyncEvent.getThreadContext())) {
                    threadContextGroupedEventsMap.put(asyncEvent.getThreadContext(), new ArrayList<>());
                }

                threadContextGroupedEventsMap.get(asyncEvent.getThreadContext()).add(asyncEvent);
            }

            try {
                for (Map.Entry<String, List<AsyncEvent>> entry : threadContextGroupedEventsMap.entrySet()) {

                    List<AsyncEvent> groupedAsyncEvents = entry.getValue();

                    List<EventInvokerEntry> eventInvokerEntries = new ArrayList<>();

                    for (AsyncEvent asyncEvent : groupedAsyncEvents) {
                        eventInvokerEntries.add(asyncEvent.getEventInvokerEntry());
                    }

                    try {
                        ThreadContextSynchronizationManager threadContextSynchronizationManager = new ThreadContextSynchronizationManager(entry.getKey());

                        threadContextSynchronizationManager.executeWithBindThreadContext(new Runnable() {
                            @Override
                            public void run() {
                                EventMethodInvoker.getInstance().invoke(eventInvokerEntries);
                            }
                        });

                    } finally {
                        for (AsyncEvent asyncEvent : groupedAsyncEvents) {
                            asyncEvent.clear();
                        }
                    }
                }
            } finally {
                asyncEvents.clear();
            }
        } else {
            logger.debug("batch doing, seq:" + sequence + ", total events:" + asyncEvents.size());
        }
    }
}
