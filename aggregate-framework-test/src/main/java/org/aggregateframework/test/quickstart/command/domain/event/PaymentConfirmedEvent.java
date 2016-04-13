package org.aggregateframework.test.quickstart.command.domain.event;

/**
 * Created by changming.xie on 4/13/16.
 */
public class PaymentConfirmedEvent {

    private Long orderId;

    public PaymentConfirmedEvent(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
