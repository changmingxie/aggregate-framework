package org.aggregateframework.sample.hierarchicalmodel.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveAggregateRootDao;
import org.aggregateframework.sample.hierarchicalmodel.command.domain.entity.HierarchicalOrder;

/**
 * Created by changming.xie on 3/30/16.
 */
public interface HierarchicalOrderDao extends CollectiveAggregateRootDao<HierarchicalOrder,Integer> {
}
