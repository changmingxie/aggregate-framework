package org.aggregateframework.sample.quickstart.command.domain.factory;

import org.aggregateframework.sample.quickstart.command.domain.entity.OrderLine;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by changming.xie on 4/7/16.
 */
public class OrderFactory {

    public static PricedOrder buildOrder(int productId, int price, int i) {

        PricedOrder pricedOrder = new PricedOrder(getNewMerchantOrderNo(i));
        pricedOrder.addOrderLine(new OrderLine(productId, price, 1));
        return pricedOrder;
    }

    private static String getNewMerchantOrderNo(int i) {

        return String.format("OR%s-%d", System.currentTimeMillis(), ThreadLocalRandom.current().nextLong());
//        return String.format("OR%s-%d", String.valueOf(System.currentTimeMillis() / 1000 / 60), i);
    }
}
