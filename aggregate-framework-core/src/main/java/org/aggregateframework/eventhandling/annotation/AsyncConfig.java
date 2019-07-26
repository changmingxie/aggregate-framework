package org.aggregateframework.eventhandling.annotation;

/**
 * Created by changming.xie on 12/19/17.
 */
public @interface AsyncConfig {

    int disruptorRingBufferSize() default 2048;
}
