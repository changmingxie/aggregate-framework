package org.aggregateframework.test;

import org.aggregateframework.test.command.domain.entity.Order;
import org.aggregateframework.test.command.domain.entity.Payment;
import org.aggregateframework.test.command.domain.entity.SeatAvailability;
import org.aggregateframework.test.command.domain.entity.UserShardingId;

import java.math.BigDecimal;

/**
 * Created by changming.xie on 2/5/16.
 */
public abstract class OrderTestCase extends AbstractTestCase {


    protected Order buildOrder() {
        Order order = new Order();
        order.setId(new UserShardingId(100));
        order.updateContent("test");
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal(100));
        order.updatePayment(payment);

        SeatAvailability seatAvailability = new SeatAvailability();

        seatAvailability.setQuantity(1000);
        seatAvailability.setPayment(payment);
        order.getSeatAvailabilities().add(seatAvailability);
        seatAvailability.setOrder(order);

        SeatAvailability seatAvailability2 = new SeatAvailability();
        seatAvailability2.setQuantity(2000);
        seatAvailability2.setPayment(payment);
        order.getSeatAvailabilities().add(seatAvailability2);
        seatAvailability2.setOrder(order);

        return order;
    }

    protected Order buildOrderWithDifferentPayments() {
        Order order = new Order();
        order.setId(new UserShardingId(100));
        order.updateContent("test");
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal(100));
        order.updatePayment(payment);

        SeatAvailability seatAvailability = new SeatAvailability();

        seatAvailability.setQuantity(1000);
        payment = new Payment();
        payment.setAmount(new BigDecimal(200));
        seatAvailability.setPayment(payment);
        order.getSeatAvailabilities().add(seatAvailability);
        seatAvailability.setOrder(order);

        SeatAvailability seatAvailability2 = new SeatAvailability();
        seatAvailability2.setQuantity(2000);

        payment = new Payment();
        payment.setAmount(new BigDecimal(300));
        seatAvailability2.setPayment(payment);
        order.getSeatAvailabilities().add(seatAvailability2);
        seatAvailability2.setOrder(order);

        return order;
    }

}
