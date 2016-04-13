package org.aggregateframework.test.complexmodel.command.domain.event;

import org.aggregateframework.test.complexmodel.command.domain.entity.BookingOrder;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-10-9
 * Time: 下午3:10
 * To change this template use File | Settings | File Templates.
 */
public class OrderCreatedEvent implements Serializable {

    private static final long serialVersionUID = 6310942080650856566L;
    private BookingOrder bookingOrder;

    public OrderCreatedEvent(BookingOrder bookingOrder) {
        this.bookingOrder = bookingOrder;
    }

    public BookingOrder getBookingOrder() {
        return bookingOrder;
    }
}
