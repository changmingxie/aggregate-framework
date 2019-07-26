package org.aggregateframework.eventbus;

import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.eventhandling.EventListener;
import org.aggregateframework.transaction.LocalTransactionExecutor;

import java.util.List;

/**
 * User: changming.xie
 * Date: 14-7-10
 * Time: 下午5:14
 */
public interface EventBus {
    
    public void subscribe(EventListener eventListener);

    void publishInTransaction(List<EventMessage> messages, LocalTransactionExecutor localTransactionExecutor);
}
