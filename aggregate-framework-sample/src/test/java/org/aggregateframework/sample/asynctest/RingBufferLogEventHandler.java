package org.aggregateframework.sample.asynctest;

import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceReportingEventHandler;

/**
 * Created by changming.xie on 11/23/17.
 */
public class RingBufferLogEventHandler implements
        SequenceReportingEventHandler<RingBufferEvent>, LifecycleAware {

    private static final int NOTIFY_PROGRESS_THRESHOLD = 50;
    private Sequence sequenceCallback;
    private int counter;
    private long threadId = -1;

    @Override
    public void setSequenceCallback(final Sequence sequenceCallback) {
        this.sequenceCallback = sequenceCallback;
    }

    @Override
    public void onEvent(final RingBufferEvent event, final long sequence,
                        final boolean endOfBatch) throws Exception {
        event.execute(endOfBatch);
        event.clear();

        // notify the BatchEventProcessor that the sequence has progressed.
        // Without this callback the sequence would not be progressed
        // until the batch has completely finished.
//        if (++counter > NOTIFY_PROGRESS_THRESHOLD) {
//            sequenceCallback.set(sequence);
//            counter = 0;
//        }
    }

    /**
     * Returns the thread ID of the background consumer thread, or {@code -1} if the background thread has not started
     * yet.
     * @return the thread ID of the background consumer thread, or {@code -1}
     */
    public long getThreadId() {
        return threadId;
    }

    @Override
    public void onStart() {
        threadId = Thread.currentThread().getId();
    }

    @Override
    public void onShutdown() {
    }
}
