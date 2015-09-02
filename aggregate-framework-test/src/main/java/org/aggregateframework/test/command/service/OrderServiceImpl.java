package org.aggregateframework.test.command.service;

import org.aggregateframework.test.command.domain.entity.Payment;
import org.aggregateframework.test.command.domain.entity.SeatAvailability;
import org.aggregateframework.test.dao.OrderDao;
import org.aggregateframework.test.command.domain.entity.Order;
import org.aggregateframework.test.command.domain.repository.JpaOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-9-24
 * Time: 下午12:07
 * To change this template use File | Settings | File Templates.
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    JpaOrderRepository orderRepository;

    @Autowired
    OrderDao orderDao;

    @Override
    public void add(Order order) {
        order.updateContent(new Date().toString());
        order.updatePayment(new Payment());
        order.getPayment().setAmount(BigDecimal.ONE);


        SeatAvailability seatAvailability = new SeatAvailability();
        seatAvailability.setQuantity(10);

        order.addSeatAvailability(seatAvailability);

        orderRepository.save(order);

        order.updateContent("test");

        orderRepository.save(order);
    }

    @Override
    public void add2(String as) {
        Order order = new Order();
        order.updateContent("add2");
        orderRepository.save(order);
    }
}
