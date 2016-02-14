package org.aggregateframework.eventhandling.processor.backoff;

import org.aggregateframework.eventhandling.processor.retry.RetryContext;

/**
 * Created by changming.xie on 2/2/16.
 */

public interface BackOffPolicy {

    BackOffContext start(RetryContext context);

    void backOff(BackOffContext backOffContext);

}