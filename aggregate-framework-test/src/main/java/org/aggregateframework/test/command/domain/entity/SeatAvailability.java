package org.aggregateframework.test.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleDomainObject;

public class SeatAvailability extends AbstractSimpleDomainObject<Integer> {

    private Order order;
    private int quantity;
    private Payment payment;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
