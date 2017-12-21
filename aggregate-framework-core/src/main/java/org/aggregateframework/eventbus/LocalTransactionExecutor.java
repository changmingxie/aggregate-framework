package org.aggregateframework.eventbus;

import org.aggregateframework.domainevent.EventMessage;

/**
 * Created by changming.xie on 12/20/17.
 */
public interface LocalTransactionExecutor {

    public LocalTransactionState executeLocalTransactionBranch(EventMessage[] events);
}
