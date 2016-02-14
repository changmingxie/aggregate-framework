package org.aggregateframework.retryabletest;

import org.aggregateframework.test.OrderTestCase;
import org.aggregateframework.test.command.domain.entity.Order;
import org.aggregateframework.test.command.domain.repository.JpaOrderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by changming.xie on 2/5/16.
 */
public class RetryableEventHandlerTest extends OrderTestCase {

    @Autowired
    JpaOrderRepository orderRepository;


    @Test
    public void given_a_persisted_order_with_multiple_component_when_remove_seatavailabilities_then_event_handler_with_retry_proceed() throws InterruptedException {

        //given
        Order order1 = buildOrderWithDifferentPayments();
        orderRepository.save(order1);

        orderRepository.flush();
        //when
        order1.removeSeatAvailabilities();
        orderRepository.save(order1);
        orderRepository.flush();
        //then
        Thread.sleep(2000);

        Assert.assertTrue(order1.isRecovered());


    }
}
