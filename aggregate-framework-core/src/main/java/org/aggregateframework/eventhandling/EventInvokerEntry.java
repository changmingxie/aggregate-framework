package org.aggregateframework.eventhandling;

import org.aggregateframework.eventhandling.transaction.EventParticipant;
import org.mengyun.compensable.transaction.Transaction;

import java.lang.reflect.Method;

/**
 * Created by changmingxie on 12/2/15.
 */
public class EventInvokerEntry {

    private Class payloadType;
    private Method method;
    private Object target;
    private Object[] params;

    private Transaction<EventParticipant> transaction;

    public EventInvokerEntry(Class payloadType, Method method, Object target, Object... params) {
        this.payloadType = payloadType;
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

    public Class getPayloadType() {
        return payloadType;
    }

    public Transaction<EventParticipant> getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction<EventParticipant> transaction) {
        this.transaction = transaction;
    }
}
