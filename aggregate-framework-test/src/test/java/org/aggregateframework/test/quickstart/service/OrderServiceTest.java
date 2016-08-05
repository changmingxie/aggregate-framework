package org.aggregateframework.test.quickstart.service;

import org.aggregateframework.eventhandling.processor.AsyncMethodInvoker;
import org.aggregateframework.test.AbstractTestCase;
import org.aggregateframework.test.quickstart.command.domain.entity.Order;
import org.aggregateframework.test.quickstart.command.domain.entity.Payment;
import org.aggregateframework.test.quickstart.command.domain.repository.PaymentRepository;
import org.aggregateframework.test.quickstart.command.service.NotifyListener;
import org.aggregateframework.test.quickstart.command.service.OrderService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by changming.xie on 4/13/16.
 */
public class OrderServiceTest extends AbstractTestCase {

    @Autowired
    NotifyListener notifyListener;

    @Autowired
    OrderService orderService;

    @Autowired
    PaymentRepository paymentRepository;

    @Test
    public void testHandleConfirmNotify() {
        Order order = orderService.placeOrder(1, 10);

        notifyListener.handleConfirmMessage(String.format("p000%s", order.getId()));

        Payment payment = paymentRepository.findByPaymentNo(String.format("p000%s", order.getId()));

        Assert.assertEquals(1, payment.getStatusId());
    }

    @Test
    public void testUpdateAndVersionChanged() {

        Order order = orderService.placeOrder(1, 10);
        notifyListener.handleConfirmMessage(String.format("p000%s", order.getId()));

        Payment payment = paymentRepository.findByPaymentNo(String.format("p000%s", order.getId()));

        paymentRepository.save(payment);

        Payment payment2 = paymentRepository.findByPaymentNo(String.format("p000%s", order.getId()));

        Assert.assertEquals(payment.getVersion(),payment2.getVersion());

    }
}
