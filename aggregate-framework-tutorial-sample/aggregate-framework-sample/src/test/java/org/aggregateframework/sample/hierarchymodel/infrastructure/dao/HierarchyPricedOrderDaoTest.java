package org.aggregateframework.sample.hierarchymodel.infrastructure.dao;

import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.complexmodel.command.infrastructure.dao.BookingOrderDao;
import org.aggregateframework.sample.hierarchicalmodel.command.domain.entity.DeliveryOrder;
import org.aggregateframework.sample.hierarchicalmodel.command.domain.entity.HierarchicalOrder;
import org.aggregateframework.sample.hierarchicalmodel.command.domain.entity.JobOrder;
import org.aggregateframework.sample.hierarchicalmodel.command.infrastructure.dao.HierarchicalOrderDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by changming.xie on 3/30/16.
 */
@Transactional
public class HierarchyPricedOrderDaoTest extends AbstractTestCase {

    @Autowired
    HierarchicalOrderDao hierarchicalOrderDao;

    @Autowired
    BookingOrderDao bookingOrderDao;


    @Test
    public void given_new_entities_when_insertAll_then_generated_id_valid() {
        List<HierarchicalOrder> entities = buildHierarchicalOrders();
        hierarchicalOrderDao.insertAll(entities);

        for (HierarchicalOrder order : entities) {
            Assert.assertTrue(order.getId() > 0);
        }
    }

    @Test
    public void given_new_entities_and_insertAll_when_findAll_then_all_found() {

        List<HierarchicalOrder> entities = buildHierarchicalOrders();
        hierarchicalOrderDao.insertAll(entities);

        List<HierarchicalOrder> hierarchicalOrders = hierarchicalOrderDao.findAll();

        for (HierarchicalOrder order : hierarchicalOrders) {
            Assert.assertTrue(order.getClass().getName().endsWith(order.getDtype()));
        }
    }

    @Test
    public void given_new_inserted_entities_when_findByIds_then_all_found() {
        List<HierarchicalOrder> entities = buildHierarchicalOrders();
        hierarchicalOrderDao.insertAll(entities);

        List<Integer> ids = new ArrayList<Integer>();

        for (HierarchicalOrder order : entities) {

            ids.add(order.getId());
        }

        List<HierarchicalOrder> hierarchicalOrders = hierarchicalOrderDao.findByIds(ids);

        for (HierarchicalOrder order : hierarchicalOrders) {

            Assert.assertTrue(order.getClass().getName().endsWith(order.getDtype()));
        }
    }

    private List<HierarchicalOrder> buildHierarchicalOrders() {
        List<HierarchicalOrder> entities = new ArrayList<HierarchicalOrder>();
        entities.add(new DeliveryOrder());
        entities.add(new JobOrder());
        entities.add(new DeliveryOrder());
        return entities;
    }
}
