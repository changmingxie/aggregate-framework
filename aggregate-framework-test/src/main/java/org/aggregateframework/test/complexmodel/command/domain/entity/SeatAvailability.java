package org.aggregateframework.test.complexmodel.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleDomainObject;

public class SeatAvailability extends AbstractSimpleDomainObject<Integer> {

    private static final long serialVersionUID = -1204283796830067140L;
    private BookingOrder bookingOrder;
    private int quantity;
    private BookingPayment bookingPayment;

    @Override
    public Integer getId() {
        return super.getId();
    }

    @Override
    public void setId(Integer id) {
        super.setId(id);
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
