package org.aggregateframework.sample.threadcontext;

import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.domain.repository.OrderRepository;
import org.aggregateframework.sample.quickstart.command.service.OrderService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ThreadContextTest extends AbstractTestCase {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void given_a_transaction_with_thread_context_when_recovery_then_loaded_transaction_has_thread_context_and_set_success() {

        List<PricedOrder> pricedOrders = new ArrayList<PricedOrder>();


        PricedOrder pricedOrder = orderService.placeOrder(1, 10, 1);

        pricedOrder.confirm(2);

        pricedOrder.confirm(2);

        pricedOrders.add(pricedOrder);

        pricedOrder = orderService.placeOrder(1, 20, 1);

        pricedOrder.confirm(2);

        pricedOrder.confirm(2);

        pricedOrders.add(pricedOrder);

        orderRepository.save(pricedOrders);

    }

    @Test
    public void given_a_transaction_with_test_tag_when_start_new_transaction_then_new_transaction_should_not_have_test_tag() throws InterruptedException {

        //set TEST FLAG

        TestThreadContextSynchronization.THREAD_CONTEXT = "TEST_FLAG1";

        PricedOrder pricedOrder = orderService.placeOrder(1, 10, 1);

        Thread.sleep(3000l);

        System.out.println("No TEST FLAG case");

        TestThreadContextSynchronization.THREAD_CONTEXT = null;

        pricedOrder = orderService.placeOrder(1, 10, 1);

    }
}
