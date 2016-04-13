package org.aggregateframework.test.hierarchicalmodel.command.infrastructure.dao;

import org.aggregateframework.dao.AggregateRootDao;
import org.aggregateframework.dao.CollectiveAggregateRootDao;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.DeliveryOrder;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.HierarchicalOrder;

/**
 * Created by changming.xie on 3/30/16.
 */
public interface HierarchicalOrderDao extends CollectiveAggregateRootDao<HierarchicalOrder,Integer> {
}
