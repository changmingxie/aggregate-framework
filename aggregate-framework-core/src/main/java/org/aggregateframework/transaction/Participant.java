package org.aggregateframework.transaction;

import org.aggregateframework.xid.TransactionXid;

/**
 * Created by changming.xie on 8/23/17.
 */
public class Participant {

    private static final long serialVersionUID = 2869353273179002268L;

    private TransactionXid xid;

    private Invocation invocation;

    public Participant(Invocation invocation) {
        this.xid = new TransactionXid();
        this.invocation = invocation;
    }

    public Participant getParent() {
        return null;
    }

    public void proceed() {
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
