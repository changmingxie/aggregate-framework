package org.aggregateframework.eventhandling;

/**
 * User: changming.xie
 * Date: 14-7-10
 * Time: 下午1:40
 */
public interface SimpleEventListenerProxy extends EventListener {
    Class<?> getTargetType();
}
