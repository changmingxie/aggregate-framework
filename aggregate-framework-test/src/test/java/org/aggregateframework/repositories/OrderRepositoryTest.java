package org.aggregateframework.repositories;

import org.aggregateframework.context.DomainObjectUtils;
import org.aggregateframework.test.AbstractTestCase;
import org.aggregateframework.test.command.domain.entity.Order;
import org.aggregateframework.test.command.domain.entity.Payment;
import org.aggregateframework.test.command.domain.entity.SeatAvailability;
import org.aggregateframework.test.command.domain.entity.UserShardingId;
import org.aggregateframework.test.command.domain.repository.JpaOrderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * User: changming.xie
 * Date: 14-6-23
 * Time: 下午4:30
 */
public class OrderRepositoryTest extends AbstractTestCase {

    @Autowired
    JpaOrderRepository orderRepository;


    private Order buildOrder() {
        Order order = new Order();
        order.setId(new UserShardingId(100));
        order.updateContent("test");
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal(100));
        order.updatePayment(payment);

        SeatAvailability seatAvailability = new SeatAvailability();

        seatAvailability.setQuantity(1000);
        seatAvailability.setPayment(payment);
        order.getSeatAvailabilities().add(seatAvailability);
        seatAvailability.setOrder(order);

        SeatAvailability seatAvailability2 = new SeatAvailability();
        seatAvailability2.setQuantity(2000);
        seatAvailability2.setPayment(payment);
        order.getSeatAvailabilities().add(seatAvailability2);
        seatAvailability2.setOrder(order);

        return order;
    }

    private Order buildOrderWithDifferentPayments() {
        Order order = new Order();
        order.setId(new UserShardingId(100));
        order.updateContent("test");
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal(100));
        order.updatePayment(payment);

        SeatAvailability seatAvailability = new SeatAvailability();

        seatAvailability.setQuantity(1000);
        payment = new Payment();
        payment.setAmount(new BigDecimal(200));
        seatAvailability.setPayment(payment);
        order.getSeatAvailabilities().add(seatAvailability);
        seatAvailability.setOrder(order);

        SeatAvailability seatAvailability2 = new SeatAvailability();
        seatAvailability2.setQuantity(2000);

        payment = new Payment();
        payment.setAmount(new BigDecimal(300));
        seatAvailability2.setPayment(payment);
        order.getSeatAvailabilities().add(seatAvailability2);
        seatAvailability2.setOrder(order);

        return order;
    }

    @Test
    public void given_a_new_order_with_multiple_component_when_save_then_order_persisted_with_few_sqls() {

        //given
        Order order1 = buildOrderWithDifferentPayments();

        //when
        orderRepository.save(order1);

        //then
        Order foundOrder1 = orderRepository.findOne(order1.getId());
        Assert.assertNotNull(foundOrder1.getId());
    }

    @Test
    @Transactional
    public void given_a_new_order_with_multiple_component_when_update_components_then_order_persisted_with_few_sqls() {

        //given
        Order order1 = buildOrderWithDifferentPayments();

        //when
        orderRepository.save(order1);
        orderRepository.flush();

        //then
        Order foundOrder1 = orderRepository.findOne(order1.getId());

        foundOrder1.getSeatAvailabilities().get(0).setQuantity(200);

        SeatAvailability seatAvailability = new SeatAvailability();

        seatAvailability.setQuantity(2000);
        foundOrder1.addSeatAvailability(seatAvailability);

        orderRepository.save(foundOrder1);
        orderRepository.flush();

        Order foundOrder2 = orderRepository.findOne(order1.getId());
        Assert.assertTrue(foundOrder2.getSeatAvailabilities().size() == 3);
    }

    @Test
    @Transactional
    public void given_a_new_order_with_multiple_component_when_remove_components_then_order_persisted_with_few_sqls() {

        //given
        Order order1 = buildOrder();

        //when
        orderRepository.save(order1);
        orderRepository.flush();

        //then
        Order foundOrder1 = orderRepository.findOne(order1.getId());

        foundOrder1.removeSeatAvailability(foundOrder1.getSeatAvailabilities().get(0));
        foundOrder1.removeSeatAvailability(foundOrder1.getSeatAvailabilities().get(0));


        orderRepository.save(foundOrder1);
        orderRepository.flush();

        Order foundOrder2 = orderRepository.findOne(order1.getId());
        Assert.assertTrue(foundOrder2.getSeatAvailabilities().size() == 0);
    }


    @Test
    @Transactional
    public void given_a_persisted_order_when_update_component_then_the_version_of_root_increment() {

        //given
        Order order = buildOrder();
        orderRepository.save(order);
        orderRepository.flush();


        //when
        Order foundOrder = orderRepository.findOne(order.getId());
        Long version = foundOrder.getVersion();
        foundOrder.getPayment().setAmount(new BigDecimal("102"));
        orderRepository.save(foundOrder);
        orderRepository.flush();

        //then
        Assert.assertEquals(version + 1, foundOrder.getVersion());
    }

    @Test
    @Transactional
    @Rollback(false)
    public void given_a_new_order_when_transactional_save_and_flush_then_the_id_generated() {

        //given
        Order order = buildOrder();

        //when
        orderRepository.save(order);
        orderRepository.flush();

        //then
        Assert.assertTrue(order.getId() != null);

    }


    @Test
    @Transactional
    public void given_a_persisted_order_when_transactional_update_the_root_then_order_updated() {

        //given
        Order order = buildOrder();
        orderRepository.save(order);
        orderRepository.flush();


        //when
        Order expectedOrder = orderRepository.findOne(order.getId());
        expectedOrder.updateContent("test 2 update");
        orderRepository.save(expectedOrder);
        orderRepository.flush();

        //then
        Order foundOrder = orderRepository.findOne(order.getId());
        Assert.assertEquals(expectedOrder.getContent(), foundOrder.getContent());
    }

    @Test
    @Transactional
    public void given_a_persisted_order_when_transactional_remove_component_then_order_updated() {

        //given
        Order order = buildOrder();
        orderRepository.save(order);
        orderRepository.flush();


        //when
        Order expectedOrder = orderRepository.findOne(order.getId());
        expectedOrder.updatePayment(null);
        List<SeatAvailability> seatAvailabilities = expectedOrder.getSeatAvailabilities();
        expectedOrder.removeSeatAvailability(seatAvailabilities.get(0));
        orderRepository.save(expectedOrder);
        orderRepository.flush();

        //then
        Order foundOrder = orderRepository.findOne(order.getId());
        Assert.assertNull(foundOrder.getPayment());
        Assert.assertEquals(expectedOrder.getSeatAvailabilities().size(), foundOrder.getSeatAvailabilities().size());
    }

    @Test
    @Transactional
    public void given_a_persisted_order_when_transactional_update_component_then_order_updated() {

        //given
        Order order = buildOrder();
        orderRepository.save(order);
        orderRepository.flush();

        //when
        Order expectedOrder = orderRepository.findOne(order.getId());

        List<SeatAvailability> seatAvailabilities = expectedOrder.getSeatAvailabilities();

        seatAvailabilities.get(0).setQuantity(10010);
        seatAvailabilities.get(1).setQuantity(10010);

        orderRepository.save(expectedOrder);

        orderRepository.flush();

        //then
        Order foundOrder = orderRepository.findOne(order.getId());

        for (SeatAvailability seatAvailability : foundOrder.getSeatAvailabilities()) {
            Assert.assertEquals(10010, seatAvailability.getQuantity());
        }
    }

    @Test
    public void given_a_prisisted_order_when_delete_then_order_removed() {

        //given
        Order order = buildOrder();
        order.updateContent("test");
        orderRepository.save(order);
        //when
        orderRepository.delete(order);

        //then
        Order foundOrder = orderRepository.findOne(order.getId());
        Assert.assertNull(foundOrder);
    }


    @Test
    public void given_a_new_order_when_save_then_order_persisted() {

        //given
        Order order = buildOrder();
        order.updateContent("test");
        //when
        orderRepository.save(order);

        //then
        Order foundOrder = orderRepository.findOne(order.getId());
        Assert.assertNotNull(foundOrder.getId());
    }

    @Test
    public void given_a_persisted_order_when_update_content_then_domain_event_fired() {

        //given
        Order order = buildOrder();
        order.updateContent("test");

        //when
        orderRepository.save(order);

        //then
        //check OrderHandler.handleOrderUpdatedEvent called
    }

    @Test
    public void given_a_persisted_order_when_update_root_then_root_updated() {

        //given
        Order order = buildOrder();
        orderRepository.save(order);

        //when
        Order expectedOrder = orderRepository.findOne(order.getId());
        expectedOrder.updateContent("test 2 update");
        orderRepository.save(expectedOrder);

        //then
        Order foundOrder = orderRepository.findOne(order.getId());
        Assert.assertNotNull(foundOrder.getId());
        Assert.assertEquals(expectedOrder.getContent(), foundOrder.getContent());
    }

    @Test
    public void given_a_persisted_order_when_remove_component_then_root_updated() {

        //given
        Order order = buildOrder();
        orderRepository.save(order);

        //when
        Order expectedOrder = orderRepository.findOne(order.getId());

        expectedOrder.updatePayment(null);

        List<SeatAvailability> seatAvailabilities = expectedOrder.getSeatAvailabilities();

        expectedOrder.removeSeatAvailability(seatAvailabilities.get(0));

        orderRepository.save(expectedOrder);

        //then
        Order foundOrder = orderRepository.findOne(order.getId());

        Assert.assertNull(foundOrder.getPayment());
        Assert.assertEquals(expectedOrder.getSeatAvailabilities().size(), foundOrder.getSeatAvailabilities().size());
    }

    @Test
    public void given_a_persisted_order_when_update_component_then_order_updated() {

        //given
        Order order = buildOrder();
        orderRepository.save(order);
        orderRepository.flush();

        //when
        Order expectedOrder = orderRepository.findOne(order.getId());

        List<SeatAvailability> seatAvailabilities = expectedOrder.getSeatAvailabilities();

        seatAvailabilities.get(0).setQuantity(10010);
        seatAvailabilities.get(1).setQuantity(10010);

        orderRepository.save(expectedOrder);

        //then
        Order foundOrder = orderRepository.findOne(order.getId());

        for (SeatAvailability seatAvailability : foundOrder.getSeatAvailabilities()) {
            Assert.assertEquals(10010, seatAvailability.getQuantity());
        }
    }

    @Test
    @Transactional
    public void given_a_persisted_order_when_find_by_id_with_transactional_then_found() {
        //given
        Order order = buildOrder();
        orderRepository.save(order);
        orderRepository.flush();

        //when
        Order foundOrder = orderRepository.findOne(order.getId());

        //then
        Assert.assertTrue(foundOrder.getId().equals(order.getId()));
    }

    @Test
    @Transactional
    public void given_a_persisted_order_when_find_by_id_multiple_times_with_transactional_then_found_orders_equals() {
        //given
        Order order = buildOrder();
        orderRepository.save(order);
        orderRepository.flush();

        //when
        Order foundOneOrder = orderRepository.findOne(order.getId());

        Order foundOtherOrder = orderRepository.findOne(order.getId());

        //then
        Assert.assertTrue(DomainObjectUtils.equal(foundOneOrder, foundOtherOrder));
    }


    @Test
    public void given_a_persisted_order_when_find_by_id_then_found() {
        Order order = buildOrder();
        orderRepository.save(order);

        Order foundOrder = orderRepository.findOne(order.getId());

        Assert.assertTrue(foundOrder.getId().equals(order.getId()));
    }

    @Test
    public void given_a_persisted_order_when_find_by_id_multiple_times_then_found_orders_equals() {
        //given
        Order order = buildOrder();
        orderRepository.save(order);
        orderRepository.flush();

        //when
        Order foundOneOrder = orderRepository.findOne(order.getId());

        Order foundOtherOrder = orderRepository.findOne(order.getId());

        //then
        Assert.assertTrue(DomainObjectUtils.equal(foundOneOrder, foundOtherOrder));
    }


    @Test
    @Transactional
    public void given_a_persisted_order_when_update_root_then_returned_object_is_the_same_with_orignal() {
        //given
        Order order = new Order();
        order.updateContent("test");
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal(100));
        order.updatePayment(payment);

        Order savedOrder = orderRepository.save(order);
        orderRepository.flush();

        //when
        savedOrder.updateContent("test2");
        Order updatedOrder = orderRepository.save(savedOrder);
        orderRepository.flush();

        //then
        Assert.assertTrue(updatedOrder.getContent().equals(order.getContent()));
    }

    @Test
    @Transactional
    public void given_a_new_order_and_seatavaibility_reference_the_same_payment_when_save_then_the_payment_persisted_correctly() {
        Order order = buildOrder();

        orderRepository.save(order);
        orderRepository.flush();
        Order foundOrder = orderRepository.findOne(order.getId());

        Assert.assertEquals(foundOrder.getPayment(), foundOrder.getSeatAvailabilities().get(0).getPayment());
    }
}
