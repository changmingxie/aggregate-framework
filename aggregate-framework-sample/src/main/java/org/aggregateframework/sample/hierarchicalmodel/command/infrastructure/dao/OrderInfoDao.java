package org.aggregateframework.sample.hierarchicalmodel.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveDomainObjectDao;
import org.aggregateframework.sample.hierarchicalmodel.command.domain.entity.OrderInfo;

/**
 * Created by changming.xie on 3/30/16.
 */
public interface OrderInfoDao extends CollectiveDomainObjectDao<OrderInfo, Integer> {
}
