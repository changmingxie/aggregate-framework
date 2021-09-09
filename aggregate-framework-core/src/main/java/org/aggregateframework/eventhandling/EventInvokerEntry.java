package org.aggregateframework.eventhandling;

import org.aggregateframework.eventhandling.transaction.EventParticipant;
import org.aggregateframework.transaction.Transaction;

import java.lang.reflect.Method;

/**
 * Created by changmingxie on 12/2/15.
 */
public class EventInvokerEntry implements Comparable<EventInvokerEntry> {
    
    private final Class  payloadType;
    private final Method method;
    private final Object target;
    private final Object[] params;
    private final int      order;
    
    private Transaction<EventParticipant> transaction;
    
    public EventInvokerEntry(Class payloadType, Method method, Object target, int order, Object... params) {
        this.payloadType = payloadType;
        this.method      = method;
        this.target      = target;
        this.order       = order;
        this.params      = params;
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
    
    public int getOrder() {
        return order;
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
    
    @Override
    public int compareTo(EventInvokerEntry o) {
        return Integer.compare(order, o.order);
    }
}
