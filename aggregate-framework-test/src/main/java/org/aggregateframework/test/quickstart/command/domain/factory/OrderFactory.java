package org.aggregateframework.test.quickstart.command.domain.factory;

import org.aggregateframework.test.quickstart.command.domain.entity.Order;
import org.aggregateframework.test.quickstart.command.domain.entity.OrderLine;

/**
 * Created by changming.xie on 4/7/16.
 */
public class OrderFactory {

    public static Order buildOrder(int productId, int price) {

        Order order = new Order(getNewMerchantOrderNo());
        order.addOrderLine(new OrderLine(productId, price,1));
        return order;
    }

    private static String getNewMerchantOrderNo() {
        return String.format("OR%s", System.currentTimeMillis());
    }
}
