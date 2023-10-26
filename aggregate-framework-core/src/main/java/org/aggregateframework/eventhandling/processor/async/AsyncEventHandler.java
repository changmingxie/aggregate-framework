/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.aggregateframework.eventhandling.processor.async;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import org.aggregateframework.eventhandling.processor.EventMethodInvoker;
import org.aggregateframework.threadcontext.ThreadContextSynchronizationManager;

/**
 * This event handler gets passed messages from the RingBuffer as they become
 * available. Processing of these messages is done in a separate thread,
 * controlled by the {@code Executor} passed to the {@code Disruptor}
 * constructor.
 */
public class AsyncEventHandler implements
        EventHandler<AsyncEvent>, WorkHandler<AsyncEvent> {

    private final long ordinal;
    private final long workPoolSize;

    public AsyncEventHandler(long ordinal, long workPoolSize) {
        this.ordinal = ordinal;
        this.workPoolSize = workPoolSize;
    }

    @Override
    public void onEvent(final AsyncEvent event, final long sequence,
                        final boolean endOfBatch) throws Exception {

        if ((sequence % workPoolSize) == ordinal) {

            try {
                doInvoke(event);
            } finally {
                event.clear();
            }
        }
    }

    @Override
    public void onEvent(AsyncEvent event) throws Exception {
        try {
            doInvoke(event);
        } finally {
            event.clear();
        }
    }

    protected void doInvoke(AsyncEvent event) {
        ThreadContextSynchronizationManager threadContextSynchronizationManager = new ThreadContextSynchronizationManager(event.getThreadContext());

        threadContextSynchronizationManager.executeWithBindThreadContext(new Runnable() {
            @Override
            public void run() {
                EventMethodInvoker.getInstance().invoke(event.getEventInvokerEntry());
            }
        });
    }
}
