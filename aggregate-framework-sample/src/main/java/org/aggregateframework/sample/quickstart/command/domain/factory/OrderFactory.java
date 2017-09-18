package org.aggregateframework.sample.quickstart.command.domain.factory;

import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.domain.entity.OrderLine;

/**
 * Created by changming.xie on 4/7/16.
 */
public class OrderFactory {

    public static PricedOrder buildOrder(int productId, int price) {

        PricedOrder pricedOrder = new PricedOrder(getNewMerchantOrderNo());
        pricedOrder.addOrderLine(new OrderLine(productId, price,1));
        return pricedOrder;
    }

    private static String getNewMerchantOrderNo() {
        return String.format("OR%s", System.currentTimeMillis());
    }
}
