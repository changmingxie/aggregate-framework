package org.aggregateframework.sample.cacheintegration;

import org.aggregateframework.cache.CaffeineL2Cache;
import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.service.OrderService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.Collection;

public class CaffeinelL2CacheTest extends AbstractTestCase {

    @Autowired
    OrderService orderService;

    @Autowired
    @Qualifier("pricedOrderLocalL2Cache")
    CaffeineL2Cache caffeineL2Cache;

    @Test
    public void given_a_presisted_order_when_write_to_L2_cache_and_sleep_expire_time_then_get_null() throws InterruptedException {

        PricedOrder pricedOrder = orderService.placeOrder(1, 10, 1);


        caffeineL2Cache.write(Arrays.asList(pricedOrder));

        PricedOrder foundPricedOrder = (PricedOrder) caffeineL2Cache.findOne(PricedOrder.class, pricedOrder.getId());
        Assert.assertEquals(pricedOrder, foundPricedOrder);
        Thread.sleep(1000 * 3l);
        foundPricedOrder = (PricedOrder) caffeineL2Cache.findOne(PricedOrder.class, pricedOrder.getId());
        Assert.assertNull(foundPricedOrder);
    }

    @Test
    public void given_a_persisted_order_when_write_to_L2_cache_and_findall_then_can_found() {

        PricedOrder pricedOrder = orderService.placeOrder(1, 10, 1);

        caffeineL2Cache.write(Arrays.asList(pricedOrder));

        Collection<PricedOrder> pricedOrders = caffeineL2Cache.findAll(PricedOrder.class, Arrays.asList(pricedOrder.getId()));

        Assert.assertTrue(pricedOrders.size() > 0);
    }
}
