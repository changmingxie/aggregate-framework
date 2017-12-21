package org.aggregateframework.sample.quickstart.command.domain.event;

import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;

/**
 * Created by changming.xie on 4/7/16.
 */
public class OrderPlacedEvent {
    private PricedOrder pricedOrder;

    public OrderPlacedEvent(PricedOrder pricedOrder) {
        this.pricedOrder = pricedOrder;
    }

    public PricedOrder getPricedOrder() {
        return pricedOrder;
    }
}
