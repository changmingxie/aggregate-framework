package org.aggregateframework.eventhandling.processor.async;


import java.lang.reflect.Method;

/**
 * Created by changming.xie on 2/1/16.
 */
public class RetryEvent {

    private Class payloadType;

    private Method method;

    private Object target;

    private Object[] params;

    private Throwable throwable;

    public void reset(Class payloadType,Throwable throwable, Method method, Object target, Object[] params) {
        this.payloadType = payloadType;
        this.throwable = throwable;
        this.method = method;
        this.target = target;
        this.params = params;
    }

    public Method getMethod() {
        return method;
    }

    public Object getTarget() {
        return target;
    }

    public Object[] getParams() {
        return params;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Class getPayloadType() {
        return payloadType;
    }
}
