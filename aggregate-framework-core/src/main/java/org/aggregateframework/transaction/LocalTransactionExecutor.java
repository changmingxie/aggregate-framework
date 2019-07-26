package org.aggregateframework.transaction;

import org.aggregateframework.domainevent.EventMessage;

import java.util.List;

/**
 * Created by changming.xie on 12/20/17.
 */
public interface LocalTransactionExecutor {

    public LocalTransactionState executeLocalTransactionBranch(List<EventMessage> events);
}
