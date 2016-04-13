package org.aggregateframework.test.hierarchymodel.domain.repository;

import org.aggregateframework.test.AbstractTestCase;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.DeliveryOrder;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.DeliveryOrderInfo;
import org.aggregateframework.test.hierarchicalmodel.command.domain.repository.DeliveryOrderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by changming.xie on 3/30/16.
 */
public class DeliveryBookingOrderRepositoryTest extends AbstractTestCase {

    @Autowired
    DeliveryOrderRepository deliveryOrderRepository;

    @Test
    @Transactional
    public void given_new_order_when_save_and_find_then_found_order_with_components_fetched() {

        DeliveryOrder deliveryOrder = buildOrder();
        deliveryOrderRepository.save(deliveryOrder);
        deliveryOrderRepository.flush();

        DeliveryOrder foundOrder = deliveryOrderRepository.findOne(deliveryOrder.getId());

        Assert.assertTrue(foundOrder.getId() > 0);
        Assert.assertTrue(foundOrder.getOrderInfo() instanceof DeliveryOrderInfo);
    }

    @Test
    @Transactional
    public void given_existed_order_when_update_then_all_changes_persisted() {
        DeliveryOrder deliveryOrder = buildOrder();
        deliveryOrderRepository.save(deliveryOrder);
        deliveryOrderRepository.flush();

        DeliveryOrder foundOrder = deliveryOrderRepository.findOne(deliveryOrder.getId());

        foundOrder.getOrderInfo().setDeliveryInfo("deliveryInfo updated");
        deliveryOrderRepository.save(foundOrder);
        deliveryOrderRepository.flush();

        foundOrder = deliveryOrderRepository.findOne(deliveryOrder.getId());

        Assert.assertTrue(foundOrder.getId() > 0);
        Assert.assertTrue(foundOrder.getOrderInfo() instanceof DeliveryOrderInfo);
    }

    @Test
    @Transactional
    public void given_existed_order_when_delete_then_removed_correctly() {
        DeliveryOrder deliveryOrder = buildOrder();
        deliveryOrderRepository.save(deliveryOrder);
        deliveryOrderRepository.flush();

        deliveryOrderRepository.delete(deliveryOrder);

        deliveryOrderRepository.flush();

        DeliveryOrder foundOrder = deliveryOrderRepository.findOne(deliveryOrder.getId());

        Assert.assertNull(foundOrder);

    }

    private DeliveryOrder buildOrder() {
        DeliveryOrder deliveryOrder = new DeliveryOrder();

        deliveryOrder.setOrderInfo(new DeliveryOrderInfo());

        deliveryOrder.getOrderInfo().setDeliveryInfo("deliverinfo");

        return deliveryOrder;
    }
}
