package org.aggregateframework.sample.complexmodel.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleDomainObject;

public class SeatAvailability extends AbstractSimpleDomainObject<Integer> {

    private static final long serialVersionUID = -1204283796830067140L;
    private BookingOrder bookingOrder;
    private int quantity;
    private BookingPayment bookingPayment;

    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public BookingOrder getBookingOrder() {
        return bookingOrder;
    }

    public void setBookingOrder(BookingOrder bookingOrder) {
        this.bookingOrder = bookingOrder;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BookingPayment getBookingPayment() {
        return bookingPayment;
    }

    public void setBookingPayment(BookingPayment bookingPayment) {
        this.bookingPayment = bookingPayment;
    }


}
