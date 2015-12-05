package org.aggregateframework.test.command.eventhandlers;

import org.aggregateframework.eventhandling.EventHandler;
import org.aggregateframework.test.command.domain.repository.JpaOrderRepository;
import org.aggregateframework.test.command.domainevents.OrderCreatedEvent;
import org.aggregateframework.test.command.domainevents.OrderUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderHandler {

    @Autowired
    JpaOrderRepository jpaOrderRepository;

    @EventHandler
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        System.out.println("sync handle create event");
    }

    @EventHandler(asynchronous = true)
    public void handleOrderUpdatedEvent(OrderUpdatedEvent event) {
        System.out.println("async handle update event");
    }

    @EventHandler(postAfterTransaction = true)
    public void postHandleOrderUpdatedEvent(OrderUpdatedEvent event) {

        System.out.println("post handle update event");
    }

    @EventHandler(asynchronous = true,postAfterTransaction = true)
    public void postAfterTransactionOrderUpdatedEvent(OrderUpdatedEvent event) {
        System.out.println("post after transaction update event");
    }
}
