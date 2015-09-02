package org.aggregateframework.eventhandling;

/**
 * User: changming.xie
 * Date: 14-7-10
 * Time: 下午1:10
 */
public interface EventListener {

    void handle(EventMessage event);
}
