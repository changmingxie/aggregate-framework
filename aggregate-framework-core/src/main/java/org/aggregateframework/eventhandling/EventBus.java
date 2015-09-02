package org.aggregateframework.eventhandling;

/**
 * User: changming.xie
 * Date: 14-7-10
 * Time: 下午5:14
 */
public interface EventBus {

    public void publish(EventMessage[] events);

    public void subscribe(EventListener eventListener);
}
