package org.aggregateframework.test.quickstart.command.eventhandler;

import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.test.quickstart.command.domain.entity.Payment;
import org.aggregateframework.test.quickstart.command.domain.event.OrderPlacedEvent;
import org.aggregateframework.test.quickstart.command.domain.factory.PaymentFactory;
import org.aggregateframework.test.quickstart.command.domain.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by changming.xie on 4/7/16.
 */
@Service
public class OrderHandler {

    @Autowired
    PaymentRepository paymentRepository;

    @EventHandler
    public void handleOrderCreatedEvent(OrderPlacedEvent event) {
        System.out.println("haha");

        Payment payment = PaymentFactory.buildPayment(event.getOrder().getId(), String.format("p000%s", event.getOrder().getId()), event.getOrder().getTotalAmount());

        paymentRepository.save(payment);
    }
}