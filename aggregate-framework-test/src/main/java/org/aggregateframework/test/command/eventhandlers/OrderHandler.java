package org.aggregateframework.test.command.eventhandlers;

import org.aggregateframework.eventhandling.EventHandler;
import org.aggregateframework.test.command.domain.repository.JpaOrderRepository;
import org.aggregateframework.test.command.domainevents.OrderCreatedEvent;
import org.aggregateframework.test.command.domainevents.OrderUpdatedApplicationEvent;
import org.aggregateframework.test.command.domainevents.OrderUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderHandler {

    @Autowired
    JpaOrderRepository jpaOrderRepository;

    @EventHandler
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {

        System.out.println(event);
    }

    @EventHandler
    public void handleOrderUpdatedEvent(OrderUpdatedEvent event) {

        System.out.println(event);
    }

    @EventHandler
    public void handleOrderUpdatedApplicationEvent(OrderUpdatedApplicationEvent event) {
        System.out.println(event);
    }
}
