package org.aggregateframework.test.hierarchymodel.infrastructure.dao;

import org.aggregateframework.test.AbstractTestCase;
import org.aggregateframework.test.complexmodel.command.infrastructure.dao.BookingOrderDao;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.DeliveryOrder;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.HierarchicalOrder;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.JobOrder;
import org.aggregateframework.test.hierarchicalmodel.command.infrastructure.dao.HierarchicalOrderDao;
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
public class HierarchyOrderDaoTest extends AbstractTestCase {

    @Autowired
    HierarchicalOrderDao hierarchicalOrderDao;

    @Autowired
    BookingOrderDao bookingOrderDao;


    @Test
    public void testInsertAll() {
        List<HierarchicalOrder> entities = buildHierarchicalOrders();
        hierarchicalOrderDao.insertAll(entities);

        for (HierarchicalOrder order : entities) {
            Assert.assertTrue(order.getId() > 0);
        }
    }

    @Test
    public void testFindAll() {

        List<HierarchicalOrder> entities = buildHierarchicalOrders();
        hierarchicalOrderDao.insertAll(entities);

        List<HierarchicalOrder> hierarchicalOrders = hierarchicalOrderDao.findAll();

        for (HierarchicalOrder order : hierarchicalOrders) {
            Assert.assertTrue(order.getClass().getName().endsWith(order.getDtype()));
        }
    }

    @Test
    public void testFindByIds() {
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
