package org.aggregateframework.sample.complexmodel.command.service;

import org.aggregateframework.sample.complexmodel.command.domain.entity.BookingPayment;
import org.aggregateframework.sample.complexmodel.command.domain.entity.SeatAvailability;
import org.aggregateframework.sample.complexmodel.command.domain.entity.BookingOrder;
import org.aggregateframework.sample.complexmodel.command.domain.repository.JpaOrderRepository;
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

    @Override
    public void add(BookingOrder bookingOrder) {
        bookingOrder.updateContent(new Date().toString());
        bookingOrder.updatePayment(new BookingPayment());
        bookingOrder.getBookingPayment().setAmount(BigDecimal.ONE);


        SeatAvailability seatAvailability = new SeatAvailability();
        seatAvailability.setQuantity(10);

        bookingOrder.addSeatAvailability(seatAvailability);

        orderRepository.save(bookingOrder);

        bookingOrder.updateContent("sample");

        orderRepository.save(bookingOrder);
    }

    @Override
    public void add2(String as) {
        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.updateContent("add2");
        orderRepository.save(bookingOrder);
    }
}
