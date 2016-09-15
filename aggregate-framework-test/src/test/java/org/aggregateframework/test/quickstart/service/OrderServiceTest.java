package org.aggregateframework.test.quickstart.service;

import org.aggregateframework.test.AbstractTestCase;
import org.aggregateframework.test.quickstart.command.domain.entity.Order;
import org.aggregateframework.test.quickstart.command.domain.entity.Payment;
import org.aggregateframework.test.quickstart.command.domain.repository.PaymentRepository;
import org.aggregateframework.test.quickstart.command.service.OrderService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by changming.xie on 4/13/16.
 */
public class OrderServiceTest extends AbstractTestCase {


    @Autowired
    OrderService orderService;

    @Autowired
    PaymentRepository paymentRepository;

    @Test
    public void testUpdateAndVersionChanged() throws InterruptedException {

        Order order = orderService.placeOrder(1, 10);

        Payment payment;
        do {
            payment = paymentRepository.findByPaymentNo(String.format("p000%s", order.getId()));

            if (payment == null) {
                Thread.sleep(1000);
                continue;
            }
            paymentRepository.save(payment);
            break;
        } while (true);

        Payment payment2 = paymentRepository.findByPaymentNo(String.format("p000%s", order.getId()));

        Assert.assertEquals(payment.getVersion(), payment2.getVersion());

    }
}
