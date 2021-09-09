package org.aggregateframework.eventhandling.annotation;

/**
 * Created by changming.xie on 12/19/17.
 */
public @interface AsyncConfig {

    QueueFullPolicy queueFullPolicy() default QueueFullPolicy.SYNCHRONOUS;

    int ringBufferSize() default 4096;

    int workPoolSize() default 24;

    int maxBatchSize() default 1024;
}
