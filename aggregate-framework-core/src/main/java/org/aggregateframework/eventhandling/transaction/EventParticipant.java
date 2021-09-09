package org.aggregateframework.eventhandling.transaction;


import org.aggregateframework.transaction.Invocation;
import org.aggregateframework.transaction.Participant;
import org.aggregateframework.transaction.TransactionXid;

/**
 * Created by changming.xie on 10/26/17.
 */
public class EventParticipant implements Participant {

    private static final long serialVersionUID = 2869353273179002268L;

    private TransactionXid xid;

    private Invocation invocation;

    @Override
    public Participant getParent() {
        return null;
    }

    @Override
    public void addChild(Participant participant) {

    }

    public EventParticipant(Invocation invocation) {
        this.xid = new TransactionXid();
        this.invocation = invocation;
    }

    public void proceed() throws Throwable {
        this.invocation.proceed();
    }

    public TransactionXid getXid() {
        return xid;
    }

    public void setXid(TransactionXid xid) {
        this.xid = xid;
    }

    public Invocation getInvocation() {
        return invocation;
    }

    public void setInvocation(Invocation invocation) {
        this.invocation = invocation;
    }
}
