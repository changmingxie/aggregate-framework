package org.aggregateframework.eventhandling.annotation;

public enum QueueFullPolicy {
    SYNCHRONOUS,
    ENQUEUE,
    DISCARD;
}
