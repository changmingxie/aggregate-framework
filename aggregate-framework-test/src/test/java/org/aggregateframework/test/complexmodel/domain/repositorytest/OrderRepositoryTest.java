package org.aggregateframework.test.complexmodel.domain.repositorytest;

import org.aggregateframework.test.complexmodel.OrderTestCase;
import org.aggregateframework.test.complexmodel.command.domain.entity.BookingOrder;
import org.aggregateframework.test.complexmodel.command.domain.entity.BookingPayment;
import org.aggregateframework.test.complexmodel.command.domain.entity.SeatAvailability;
import org.aggregateframework.test.complexmodel.command.domain.repository.JpaOrderRepository;
import org.aggregateframework.utils.DomainObjectUtils;
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
public class OrderRepositoryTest extends OrderTestCase {

    @Autowired
    JpaOrderRepository orderRepository;


    @Test
    public void given_a_new_order_with_multiple_component_when_save_then_order_persisted_with_few_sqls() throws InterruptedException {

        //given
        BookingOrder bookingOrder1 = buildOrderWithDifferentPayments();

        //when
        orderRepository.save(bookingOrder1);

        //then
        BookingOrder foundBookingOrder1 = orderRepository.findOne(bookingOrder1.getId());
        Assert.assertNotNull(foundBookingOrder1.getId());
    }

    @Test
    @Transactional
    public void given_a_new_order_with_multiple_component_when_update_components_then_order_persisted_with_few_sqls() {

        //given
        BookingOrder bookingOrder1 = buildOrderWithDifferentPayments();

        //when
        orderRepository.save(bookingOrder1);
        orderRepository.flush();

        //then
        BookingOrder foundBookingOrder1 = orderRepository.findOne(bookingOrder1.getId());

        foundBookingOrder1.getSeatAvailabilities().get(0).setQuantity(200);

        SeatAvailability seatAvailability = new SeatAvailability();

        seatAvailability.setQuantity(2000);
        foundBookingOrder1.addSeatAvailability(seatAvailability);

        orderRepository.save(foundBookingOrder1);
        orderRepository.flush();

        BookingOrder foundBookingOrder2 = orderRepository.findOne(bookingOrder1.getId());
        Assert.assertTrue(foundBookingOrder2.getSeatAvailabilities().size() == 3);
    }

    @Test
    @Transactional
    public void given_a_new_order_with_multiple_component_when_remove_components_then_order_persisted_with_few_sqls() {

        //given
        BookingOrder bookingOrder1 = buildOrder();

        //when
        orderRepository.save(bookingOrder1);
        orderRepository.flush();

        //then
        BookingOrder foundBookingOrder1 = orderRepository.findOne(bookingOrder1.getId());

        foundBookingOrder1.removeSeatAvailability(foundBookingOrder1.getSeatAvailabilities().get(0));
        foundBookingOrder1.removeSeatAvailability(foundBookingOrder1.getSeatAvailabilities().get(0));


        orderRepository.save(foundBookingOrder1);
        orderRepository.flush();

        BookingOrder foundBookingOrder2 = orderRepository.findOne(bookingOrder1.getId());
        Assert.assertTrue(foundBookingOrder2.getSeatAvailabilities().size() == 0);
    }


    @Test
    @Transactional
    public void given_a_persisted_order_when_update_component_then_the_version_of_root_increment() {

        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);
        orderRepository.flush();


        //when
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());
        Long version = foundBookingOrder.getVersion();
        foundBookingOrder.getBookingPayment().setAmount(new BigDecimal("102"));
        orderRepository.save(foundBookingOrder);
        orderRepository.flush();

        //then
        Assert.assertEquals(version + 1, foundBookingOrder.getVersion());
    }

    @Test
    @Transactional
    @Rollback(false)
    public void given_a_new_order_when_transactional_save_and_flush_then_the_id_generated() {

        //given
        BookingOrder bookingOrder = buildOrder();

        //when
        orderRepository.save(bookingOrder);
        orderRepository.flush();

        //then
        Assert.assertTrue(bookingOrder.getId() != null);

    }


    @Test
    @Transactional
    public void given_a_persisted_order_when_transactional_update_the_root_then_order_updated() {

        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);
        orderRepository.flush();


        //when
        BookingOrder expectedBookingOrder = orderRepository.findOne(bookingOrder.getId());
        expectedBookingOrder.updateContent("test 2 update");
        orderRepository.save(expectedBookingOrder);
        orderRepository.flush();

        //then
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());
        Assert.assertEquals(expectedBookingOrder.getContent(), foundBookingOrder.getContent());
    }

    @Test
    @Transactional
    public void given_a_persisted_order_when_transactional_remove_component_then_order_updated() {

        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);
        orderRepository.flush();


        //when
        BookingOrder expectedBookingOrder = orderRepository.findOne(bookingOrder.getId());
        expectedBookingOrder.updatePayment(null);
        List<SeatAvailability> seatAvailabilities = expectedBookingOrder.getSeatAvailabilities();
        expectedBookingOrder.removeSeatAvailability(seatAvailabilities.get(0));
        orderRepository.save(expectedBookingOrder);
        orderRepository.flush();

        //then
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());
        Assert.assertNull(foundBookingOrder.getBookingPayment());
        Assert.assertEquals(expectedBookingOrder.getSeatAvailabilities().size(), foundBookingOrder.getSeatAvailabilities().size());
    }

    @Test
    @Transactional
    public void given_a_persisted_order_when_transactional_update_component_then_order_updated() {

        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);
        orderRepository.flush();

        //when
        BookingOrder expectedBookingOrder = orderRepository.findOne(bookingOrder.getId());

        List<SeatAvailability> seatAvailabilities = expectedBookingOrder.getSeatAvailabilities();

        seatAvailabilities.get(0).setQuantity(10010);
        seatAvailabilities.get(1).setQuantity(10010);

        orderRepository.save(expectedBookingOrder);

        orderRepository.flush();

        //then
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());

        for (SeatAvailability seatAvailability : foundBookingOrder.getSeatAvailabilities()) {
            Assert.assertEquals(10010, seatAvailability.getQuantity());
        }
    }

    @Test
    public void given_a_persisted_order_when_update_component_then_order_version_updated() {

        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);

        Long version = bookingOrder.getVersion();
        //when
        BookingOrder expectedBookingOrder = orderRepository.findOne(bookingOrder.getId());

        List<SeatAvailability> seatAvailabilities = expectedBookingOrder.getSeatAvailabilities();

        seatAvailabilities.get(0).setQuantity(10010);
        seatAvailabilities.get(1).setQuantity(10010);

        orderRepository.save(expectedBookingOrder);

        //then
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());

        for (SeatAvailability seatAvailability : foundBookingOrder.getSeatAvailabilities()) {
            Assert.assertEquals(10010, seatAvailability.getQuantity());
        }

        Assert.assertEquals(version + 1, expectedBookingOrder.getVersion());
    }

    @Test
    public void given_a_prisisted_order_when_delete_then_order_removed() {

        //given
        BookingOrder bookingOrder = buildOrder();
        bookingOrder.updateContent("test");
        orderRepository.save(bookingOrder);
        //when
        orderRepository.delete(bookingOrder);

        //then
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());
        Assert.assertNull(foundBookingOrder);
    }


    @Test
    public void given_a_new_order_when_save_then_order_persisted() {

        //given
        BookingOrder bookingOrder = buildOrder();
        bookingOrder.updateContent("test");
        //when
        orderRepository.save(bookingOrder);

        //then
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());
        Assert.assertNotNull(foundBookingOrder.getId());
    }

    @Test
    public void given_a_persisted_order_when_update_content_then_domain_event_fired() {

        //given
        BookingOrder bookingOrder = buildOrder();
        bookingOrder.updateContent("test");

        //when
        orderRepository.save(bookingOrder);

        //then
        //check OrderHandler.handleOrderUpdatedEvent called
    }

    @Test
    public void given_a_persisted_order_when_update_root_then_root_updated() {

        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);

        //when
        BookingOrder expectedBookingOrder = orderRepository.findOne(bookingOrder.getId());
        expectedBookingOrder.updateContent("test 2 update");
        orderRepository.save(expectedBookingOrder);

        //then
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());
        Assert.assertNotNull(foundBookingOrder.getId());
        Assert.assertEquals(expectedBookingOrder.getContent(), foundBookingOrder.getContent());
    }

    @Test
    public void given_a_persisted_order_when_remove_component_then_root_updated() {

        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);

        //when
        BookingOrder expectedBookingOrder = orderRepository.findOne(bookingOrder.getId());

        expectedBookingOrder.updatePayment(null);

        List<SeatAvailability> seatAvailabilities = expectedBookingOrder.getSeatAvailabilities();

        expectedBookingOrder.removeSeatAvailability(seatAvailabilities.get(0));

        orderRepository.save(expectedBookingOrder);

        //then
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());

        Assert.assertNull(foundBookingOrder.getBookingPayment());
        Assert.assertEquals(expectedBookingOrder.getSeatAvailabilities().size(), foundBookingOrder.getSeatAvailabilities().size());
    }

    @Test
    public void given_a_persisted_order_when_update_component_then_order_updated() {

        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);
        orderRepository.flush();

        //when
        BookingOrder expectedBookingOrder = orderRepository.findOne(bookingOrder.getId());

        List<SeatAvailability> seatAvailabilities = expectedBookingOrder.getSeatAvailabilities();

        seatAvailabilities.get(0).setQuantity(10010);
        seatAvailabilities.get(1).setQuantity(10010);

        orderRepository.save(expectedBookingOrder);

        //then
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());

        for (SeatAvailability seatAvailability : foundBookingOrder.getSeatAvailabilities()) {
            Assert.assertEquals(10010, seatAvailability.getQuantity());
        }
    }

    @Test
    @Transactional
    public void given_a_persisted_order_when_find_by_id_with_transactional_then_found() {
        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);
        orderRepository.flush();

        //when
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());

        //then
        Assert.assertTrue(foundBookingOrder.getId().equals(bookingOrder.getId()));
    }

    @Test
    @Transactional
    public void given_a_persisted_order_when_find_by_id_multiple_times_with_transactional_then_found_orders_equals() {
        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);
        orderRepository.flush();

        //when
        BookingOrder foundOneBookingOrder = orderRepository.findOne(bookingOrder.getId());

        BookingOrder foundOtherBookingOrder = orderRepository.findOne(bookingOrder.getId());

        //then
        Assert.assertTrue(DomainObjectUtils.equal(foundOneBookingOrder, foundOtherBookingOrder));
    }


    @Test
    public void given_a_persisted_order_when_find_by_id_then_found() {
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);

        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());

        Assert.assertTrue(foundBookingOrder.getId().equals(bookingOrder.getId()));
    }

    @Test
    public void given_a_persisted_order_when_find_by_id_multiple_times_then_found_orders_equals() {
        //given
        BookingOrder bookingOrder = buildOrder();
        orderRepository.save(bookingOrder);
        orderRepository.flush();

        //when
        BookingOrder foundOneBookingOrder = orderRepository.findOne(bookingOrder.getId());

        BookingOrder foundOtherBookingOrder = orderRepository.findOne(bookingOrder.getId());

        //then
        Assert.assertTrue(DomainObjectUtils.equal(foundOneBookingOrder, foundOtherBookingOrder));
    }


    @Test
    @Transactional
    public void given_a_persisted_order_when_update_root_then_returned_object_is_the_same_with_orignal() {
        //given
        BookingOrder bookingOrder = new BookingOrder();
        bookingOrder.updateContent("test");
        BookingPayment bookingPayment = new BookingPayment();
        bookingPayment.setAmount(new BigDecimal(100));
        bookingOrder.updatePayment(bookingPayment);

        BookingOrder savedBookingOrder = orderRepository.save(bookingOrder);
        orderRepository.flush();

        //when
        savedBookingOrder.updateContent("test2");
        BookingOrder updatedBookingOrder = orderRepository.save(savedBookingOrder);
        orderRepository.flush();

        //then
        Assert.assertTrue(updatedBookingOrder.getContent().equals(bookingOrder.getContent()));
    }

    @Test
    @Transactional
    public void given_a_new_order_and_seatavaibility_reference_the_same_payment_when_save_then_the_payment_persisted_correctly() {
        BookingOrder bookingOrder = buildOrder();

        orderRepository.save(bookingOrder);
        orderRepository.flush();
        BookingOrder foundBookingOrder = orderRepository.findOne(bookingOrder.getId());

        Assert.assertEquals(foundBookingOrder.getBookingPayment(), foundBookingOrder.getSeatAvailabilities().get(0).getBookingPayment());
    }



}
