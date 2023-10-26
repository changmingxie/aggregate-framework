package org.aggregateframework.eventhandling;

import java.lang.reflect.Method;

public interface EventHandlerListener {
    void before(Object target, Method method, Object[] params) throws Exception;

    void after(Object target, Method method, Object[] params, Exception e) throws Exception;

    boolean isActive();
}
