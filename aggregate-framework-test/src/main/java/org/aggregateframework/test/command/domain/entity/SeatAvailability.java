package org.aggregateframework.test.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleDomainObject;

public class SeatAvailability extends AbstractSimpleDomainObject<Integer> {

    private static final long serialVersionUID = -1204283796830067140L;
    private Order order;
    private int quantity;
    private Payment payment;

    @Override
    public Integer getId() {
        return super.getId();
    }

    @Override
    public void setId(Integer id) {
        super.setId(id);
    }

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
