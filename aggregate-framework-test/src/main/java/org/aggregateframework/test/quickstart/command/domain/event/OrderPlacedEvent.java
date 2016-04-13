package org.aggregateframework.test.quickstart.command.domain.event;

import org.aggregateframework.test.quickstart.command.domain.entity.Order;

/**
 * Created by changming.xie on 4/7/16.
 */
public class OrderPlacedEvent {
    private Order order;

    public OrderPlacedEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
