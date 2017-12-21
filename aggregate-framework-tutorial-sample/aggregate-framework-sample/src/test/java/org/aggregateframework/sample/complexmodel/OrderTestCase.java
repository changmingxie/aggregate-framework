package org.aggregateframework.sample.complexmodel;

import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.complexmodel.command.domain.entity.BookingOrder;
import org.aggregateframework.sample.complexmodel.command.domain.entity.BookingPayment;
import org.aggregateframework.sample.complexmodel.command.domain.entity.SeatAvailability;
import org.aggregateframework.sample.complexmodel.command.domain.entity.UserShardingId;

import java.math.BigDecimal;

/**
 * Created by changming.xie on 2/5/16.
 */
public abstract class OrderTestCase extends AbstractTestCase {


    protected BookingOrder buildOrder() {
        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.setId(new UserShardingId(100));
        bookingOrder.updateContent("sample");
        BookingPayment bookingPayment = new BookingPayment();
        bookingPayment.setAmount(new BigDecimal(100));
        bookingOrder.updatePayment(bookingPayment);

        SeatAvailability seatAvailability = new SeatAvailability();

        seatAvailability.setQuantity(1000);
        seatAvailability.setBookingPayment(bookingPayment);
        bookingOrder.getSeatAvailabilities().add(seatAvailability);
        seatAvailability.setBookingOrder(bookingOrder);

        SeatAvailability seatAvailability2 = new SeatAvailability();
        seatAvailability2.setQuantity(2000);
        seatAvailability2.setBookingPayment(bookingPayment);
        bookingOrder.getSeatAvailabilities().add(seatAvailability2);
        seatAvailability2.setBookingOrder(bookingOrder);

        return bookingOrder;
    }

    protected BookingOrder buildOrderWithDifferentPayments() {
        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.setId(new UserShardingId(100));
        bookingOrder.updateContent("sample");
        BookingPayment bookingPayment = new BookingPayment();
        bookingPayment.setAmount(new BigDecimal(100));
        bookingOrder.updatePayment(bookingPayment);

        SeatAvailability seatAvailability = new SeatAvailability();

        seatAvailability.setQuantity(1000);
        bookingPayment = new BookingPayment();
        bookingPayment.setAmount(new BigDecimal(200));
        seatAvailability.setBookingPayment(bookingPayment);
        bookingOrder.getSeatAvailabilities().add(seatAvailability);
        seatAvailability.setBookingOrder(bookingOrder);

        SeatAvailability seatAvailability2 = new SeatAvailability();
        seatAvailability2.setQuantity(2000);

        bookingPayment = new BookingPayment();
        bookingPayment.setAmount(new BigDecimal(300));
        seatAvailability2.setBookingPayment(bookingPayment);
        bookingOrder.getSeatAvailabilities().add(seatAvailability2);
        seatAvailability2.setBookingOrder(bookingOrder);

        return bookingOrder;
    }

}
