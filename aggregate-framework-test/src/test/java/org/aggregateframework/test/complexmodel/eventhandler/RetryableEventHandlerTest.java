package org.aggregateframework.test.complexmodel.eventhandler;

import org.aggregateframework.test.complexmodel.OrderTestCase;
import org.aggregateframework.test.complexmodel.command.domain.entity.BookingOrder;
import org.aggregateframework.test.complexmodel.command.domain.repository.JpaOrderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by changming.xie on 2/5/16.
 */
public class RetryableEventHandlerTest extends OrderTestCase {

    @Autowired
    JpaOrderRepository orderRepository;


    @Test
    public void given_a_persisted_order_with_multiple_component_when_remove_seatavailabilities_then_event_handler_with_retry_proceed() throws InterruptedException {

        //given
        BookingOrder bookingOrder1 = buildOrderWithDifferentPayments();
        orderRepository.save(bookingOrder1);

        orderRepository.flush();
        //when
        bookingOrder1.removeSeatAvailabilities();
        orderRepository.save(bookingOrder1);
        orderRepository.flush();
        //then
        Thread.sleep(2000);

        Assert.assertTrue(bookingOrder1.isRecovered());


    }
}
