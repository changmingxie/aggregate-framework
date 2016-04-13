package org.aggregateframework.test.quickstart.command.eventhandler;

import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.test.quickstart.command.domain.entity.Order;
import org.aggregateframework.test.quickstart.command.domain.event.PaymentConfirmedEvent;
import org.aggregateframework.test.quickstart.command.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by changming.xie on 4/7/16.
 */
@Service
public class PaymentHandler {

    @Autowired
    OrderRepository orderRepository;

    @EventHandler
    public void handlePaymentConfirmedEvent(PaymentConfirmedEvent event) {

        Order order = orderRepository.findOne(event.getOrderId());
        order.confirm();
        orderRepository.save(order);
    }
}
