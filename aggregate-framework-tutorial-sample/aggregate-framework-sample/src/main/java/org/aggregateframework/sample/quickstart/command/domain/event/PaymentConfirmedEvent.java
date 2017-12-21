package org.aggregateframework.sample.quickstart.command.domain.event;

import java.io.Serializable;

/**
 * Created by changming.xie on 4/13/16.
 */
public class PaymentConfirmedEvent implements Serializable {

    private static final long serialVersionUID = -5599479410994660047L;
    private Long orderId;

    public PaymentConfirmedEvent(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
