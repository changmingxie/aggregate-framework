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

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default disruptor exception handler for errors that occur in the AsyncLogger background thread.
 */
public class EventProcessDefaultExceptionHandler implements ExceptionHandler<AsyncEvent> {

    static final Logger logger = LoggerFactory.getLogger(EventProcessDefaultExceptionHandler.class);

    @Override
    public void handleEventException(Throwable throwable, long sequence, AsyncEvent event) {

        final StringBuilder sb = new StringBuilder(512);
        sb.append("EventProcess error handling event seq=").append(sequence).append(", value='");
        try {
            sb.append(event);
        } catch (final Exception ignored) {
            sb.append("[ERROR calling ").append(event.getClass()).append(".toString(): ");
            sb.append(ignored).append("]");
        } finally {
            event.clear();
        }

        sb.append("':");

        logger.error(sb.toString(), throwable);
    }

    @Override
    public void handleOnStartException(final Throwable throwable) {
        logger.error("EventProcess error starting:", throwable);
    }

    @Override
    public void handleOnShutdownException(final Throwable throwable) {
        logger.error("EventProcess error  shutting down:", throwable);
    }
}
