package org.aggregateframework.test.command.domainevents;

import org.aggregateframework.test.command.domain.entity.Order;

/**
 * Created by changming.xie on 2/4/16.
 */
public class SeatAvailabilityRemovedEvent {

    Order order;

    public SeatAvailabilityRemovedEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
