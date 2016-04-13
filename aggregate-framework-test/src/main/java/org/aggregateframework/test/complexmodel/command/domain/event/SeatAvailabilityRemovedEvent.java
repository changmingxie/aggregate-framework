package org.aggregateframework.test.complexmodel.command.domain.event;

import org.aggregateframework.test.complexmodel.command.domain.entity.BookingOrder;

/**
 * Created by changming.xie on 2/4/16.
 */
public class SeatAvailabilityRemovedEvent {

    BookingOrder bookingOrder;

    public SeatAvailabilityRemovedEvent(BookingOrder bookingOrder) {
        this.bookingOrder = bookingOrder;
    }

    public BookingOrder getBookingOrder() {
        return bookingOrder;
    }
}
