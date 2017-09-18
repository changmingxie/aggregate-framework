package org.aggregateframework.sample.quickstart.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveAggregateRootDao;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;

import java.util.List;

/**
 * Created by changming.xie on 4/8/16.
 */
public interface OrderDao extends CollectiveAggregateRootDao<PricedOrder, Long> {
    List<PricedOrder> findByPrice();
}
