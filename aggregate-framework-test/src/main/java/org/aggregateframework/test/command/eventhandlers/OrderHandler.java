package org.aggregateframework.test.command.eventhandlers;

import org.aggregateframework.SystemException;
import org.aggregateframework.eventhandling.annotation.Backoff;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.annotation.Retryable;
import org.aggregateframework.test.command.domain.entity.Order;
import org.aggregateframework.test.command.domain.repository.JpaOrderRepository;
import org.aggregateframework.test.command.domainevents.OrderCreatedEvent;
import org.aggregateframework.test.command.domainevents.OrderUpdatedEvent;
import org.aggregateframework.test.command.domainevents.SeatAvailabilityRemovedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void postHandleOrderUpdatedEvent(OrderUpdatedEvent event) {
        System.out.println("post handle update event");
    }

    @EventHandler(asynchronous = true, postAfterTransaction = true)
    public void postAfterTransactionOrderUpdatedEvent(OrderUpdatedEvent event) {
        System.out.println("post after transaction update event at:" + System.currentTimeMillis());
    }

    @EventHandler(asynchronous = true, postAfterTransaction = true)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500,multiplier = 2), recoverMethod = "recoverHandleSeatAvailabilityRemovedEvent")
    public void handleSeatAvailabilityRemovedEvent(SeatAvailabilityRemovedEvent event) {
        System.out.println("handleSeatAvailabilityRemovedEvent called at:" + System.currentTimeMillis());
        throw new RuntimeException();
    }

    @EventHandler
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500,multiplier = 2), recoverMethod = "recoverHandleSeatAvailabilityRemovedEvent")
    public void syncHandleSeatAvailabilityRemovedEvent(SeatAvailabilityRemovedEvent event) {
        System.out.println("syncHandleSeatAvailabilityRemovedEvent called at:" + System.currentTimeMillis());
        throw new RuntimeException();
    }

    public void recoverHandleSeatAvailabilityRemovedEvent(SeatAvailabilityRemovedEvent event) {
        System.out.println("recoverHandleSeatAvailabilityRemovedEvent called at:" + System.currentTimeMillis());
        Order order = event.getOrder();

        order.recovered();
    }
}
