package org.aggregateframework.sample.quickstart.command.domain.event;

import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;

import java.io.Serializable;

/**
 * Created by changming.xie on 11/28/17.
 */
public class OrderConfirmedEvent implements Serializable {
    private static final long serialVersionUID = 5747983401748068456L;

    public OrderConfirmedEvent(PricedOrder pricedOrder) {
    }
}
