package org.aggregateframework.sample.quickstart.command.domain.event;

import java.io.Serializable;

/**
 * Created by changming.xie on 4/13/16.
 */
public class PaymentConfirmedEvent implements Serializable {

    private static final long serialVersionUID = -5599479410994660047L;
    private String paymentNo;

    public PaymentConfirmedEvent(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public String getPaymentNo() {
        return paymentNo;
    }
}
