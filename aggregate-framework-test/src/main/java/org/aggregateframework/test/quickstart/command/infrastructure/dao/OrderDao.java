package org.aggregateframework.test.quickstart.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveAggregateRootDao;
import org.aggregateframework.test.quickstart.command.domain.entity.Order;

/**
 * Created by changming.xie on 4/8/16.
 */
public interface OrderDao extends CollectiveAggregateRootDao<Order, Long> {
}
