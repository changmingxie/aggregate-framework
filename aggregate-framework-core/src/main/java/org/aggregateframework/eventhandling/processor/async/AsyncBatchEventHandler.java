package org.aggregateframework.eventhandling.processor.async;

import com.lmax.disruptor.EventHandler;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.processor.EventMethodInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

            List<EventInvokerEntry> eventInvokerEntries = new ArrayList<>();

            for (AsyncEvent asyncEvent : asyncEvents) {
                eventInvokerEntries.add(asyncEvent.getEventInvokerEntry());
            }

            try {
                EventMethodInvoker.getInstance().invoke(eventInvokerEntries);
            } finally {
                for (AsyncEvent asyncEvent : asyncEvents) {
                    asyncEvent.clear();
                }
                asyncEvents.clear();
            }
        } else {
            logger.debug("batch doing, seq:" + sequence + ", total events:" + asyncEvents.size());
        }
    }


//    @Override
//    public void onEvent(AsyncEvent event, long sequence, boolean endOfBatch) throws Exception {
//
//        asyncEvents.add(event);
//
//        org.aggregateframework.eventhandling.annotation.EventHandler eventHandler = event.getEventInvokerEntry().getMethod().getAnnotation(org.aggregateframework.eventhandling.annotation.EventHandler.class);
//
//        int maxBatchSize = eventHandler.asyncConfig().maxBatchSize();
//
//        if (asyncEvents.size() >= maxBatchSize || endOfBatch) {
//
//            logger.debug("batch done, seq:" + sequence + ", total events:" + asyncEvents.size());
//
//            Collection aggregateParams = (Collection) asyncEvents.get(0).getEventInvokerEntry().getParams()[0];
//
//            for (int i = 1; i < asyncEvents.size(); i++) {
//                aggregateParams.addAll((Collection) asyncEvents.get(i).getEventInvokerEntry().getParams()[0]);
//            }
//
//            EventInvokerEntry currentEventInvokerEntry = event.getEventInvokerEntry();
//            EventInvokerEntry batchEventInvokerEntry = new EventInvokerEntry(
//                    currentEventInvokerEntry.getPayloadType(),
//                    currentEventInvokerEntry.getMethod(),
//                    currentEventInvokerEntry.getTarget(),
//                    currentEventInvokerEntry.getOrder(),
//                    aggregateParams
//            );
//
//            try {
//                EventMethodInvoker.getInstance().invoke(batchEventInvokerEntry);
//            } finally {
//                for (AsyncEvent asyncEvent : asyncEvents) {
//                    asyncEvent.clear();
//                }
//                asyncEvents.clear();
//            }
//        } else {
//            logger.debug("batch doing, seq:" + sequence + ", total events:" + asyncEvents.size());
//        }
//    }
}
