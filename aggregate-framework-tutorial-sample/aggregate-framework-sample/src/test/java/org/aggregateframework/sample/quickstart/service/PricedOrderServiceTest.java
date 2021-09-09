package org.aggregateframework.sample.quickstart.service;

import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.domain.repository.OrderRepository;
import org.aggregateframework.sample.quickstart.command.domain.repository.PaymentRepository;
import org.aggregateframework.sample.quickstart.command.service.OrderService;
import org.aggregateframework.utils.KryoSerializationUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * Created by changming.xie on 4/13/16.
 */
public class PricedOrderServiceTest extends AbstractTestCase {


    @Autowired
    OrderService orderService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @Transactional
    @Rollback(false)
    public void given_existed_order_when_update_component_then_version_updated() throws InterruptedException {

        PricedOrder pricedOrder = orderService.placeOrder(1, 10,1);

        orderRepository.flush();

        PricedOrder foundOrder = orderRepository.findOne(pricedOrder.getId());

        Assert.assertTrue(pricedOrder.getVersion() == foundOrder.getVersion());

        foundOrder.getOrderLines().get(0).setPrice(20 + new Random().nextInt());

        orderRepository.save(foundOrder);

        orderRepository.flush();

        Assert.assertEquals(2, foundOrder.getVersion());

    }

    @Before
    public void before() {
        KryoSerializationUtils.getInstance();
    }

    @Test
    public void given_priced_order_when_clone_then_println_out_the_cost_time() {

        PricedOrder pricedOrder = orderService.placeOrder(1, 10,1);


        Long kryoStartTime = System.currentTimeMillis();
        PricedOrder clonedOrder = KryoSerializationUtils.clone(pricedOrder);
        System.out.println("kryo cost time:" + (System.currentTimeMillis() - kryoStartTime));

        kryoStartTime = System.currentTimeMillis();
        clonedOrder = KryoSerializationUtils.clone(pricedOrder);
        System.out.println("kryo cost time:" + (System.currentTimeMillis() - kryoStartTime));

        Assert.assertEquals(clonedOrder.getId(), pricedOrder.getId());

        Long jdkStartTime = System.currentTimeMillis();
        PricedOrder clonedOrder2 = SerializationUtils.clone(pricedOrder);
        System.out.println("jdk cost time:" + (System.currentTimeMillis() - jdkStartTime));

        Long jdkStartTime2 = System.currentTimeMillis();
        PricedOrder clonedOrder3 = SerializationUtils.clone(pricedOrder);
        System.out.println("jdk cost time:" + (System.currentTimeMillis() - jdkStartTime2));


        Assert.assertEquals(clonedOrder2.getId(), pricedOrder.getId());

//        for (; ; ) ;
    }


}
