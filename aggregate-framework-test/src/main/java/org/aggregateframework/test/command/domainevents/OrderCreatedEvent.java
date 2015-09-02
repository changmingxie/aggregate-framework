package org.aggregateframework.test.command.domainevents;

import org.aggregateframework.test.command.domain.entity.Order;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-10-9
 * Time: 下午3:10
 * To change this template use File | Settings | File Templates.
 */
public class OrderCreatedEvent implements Serializable {

    private Order order;

    public OrderCreatedEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
