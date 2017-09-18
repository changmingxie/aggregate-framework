package org.aggregateframework.retry.backoff;

import org.aggregateframework.retry.RetryContext;

/**
 * Created by changming.xie on 2/2/16.
 */

public interface BackOffPolicy {

    BackOffContext start(RetryContext context);

    void backOff(BackOffContext backOffContext);

}