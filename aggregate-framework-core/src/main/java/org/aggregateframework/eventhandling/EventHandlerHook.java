package org.aggregateframework.eventhandling;

import org.mengyun.commons.bean.FactoryBuilder;
import org.mengyun.compensable.transaction.Invocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventHandlerHook {
    public static final EventHandlerHook INSTANCE = new EventHandlerHook();

    private List<EventHandlerListener> listeners = new ArrayList<EventHandlerListener>();

    public void addListener(EventHandlerListener listener) {
        listeners.add(listener);
    }

    public void clear() {
        listeners.clear();
    }

    public void beforeEventHandler(Invocation invocation) throws Exception {

        if (listeners.isEmpty()) {
            return;
        }

        Object target = null;
        Method method = null;

        for (EventHandlerListener listener : listeners) {
            if (listener.isActive()) {
                if (target == null) {
                    target = FactoryBuilder.factoryOf(invocation.getTargetClass()).getInstance();
                }

                if (method == null) {
                    method = target.getClass().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                }

                listener.before(target, method, invocation.getArgs());
            }
        }
    }

    public void afterEventHandler(Invocation invocation, Exception e) throws Exception {

        if (listeners.isEmpty()) {
            return;
        }

        Object target = null;
        Method method = null;

        for (EventHandlerListener listener : listeners) {
            if (listener.isActive()) {
                if (target == null) {
                    target = FactoryBuilder.factoryOf(invocation.getTargetClass()).getInstance();
                }

                if (method == null) {
                    method = target.getClass().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                }

                listener.after(target, method, invocation.getArgs(), e);
            }
        }
    }

    public void beforeEventHandler(Object target, Method method, Object[] params) throws Exception {

        if (listeners.isEmpty()) {
            return;
        }

        for (EventHandlerListener listener : listeners) {
            if (listener.isActive()) {
                listener.before(target, method, params);
            }
        }
    }

    public void afterEventHandler(Object target, Method method, Object[] params, Exception e) throws Exception {

        if (listeners.isEmpty()) {
            return;
        }

        for (EventHandlerListener listener : listeners) {
            if (listener.isActive()) {
                listener.after(target, method, params, e);
            }
        }
    }
}
