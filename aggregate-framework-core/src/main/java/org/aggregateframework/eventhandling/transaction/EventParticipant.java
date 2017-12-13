package org.aggregateframework.eventhandling.transaction;

import org.mengyun.compensable.transaction.Invocation;
import org.mengyun.compensable.transaction.Participant;
import org.mengyun.compensable.transaction.TransactionXid;

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
}
