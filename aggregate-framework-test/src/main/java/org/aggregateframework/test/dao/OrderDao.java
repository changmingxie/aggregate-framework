package org.aggregateframework.test.dao;

import org.aggregateframework.dao.CollectiveAggregateRootDao;
import org.aggregateframework.test.command.domain.entity.Order;
import org.aggregateframework.dao.AggregateRootDao;
import org.aggregateframework.test.command.domain.entity.UserShardingId;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-9-16
 * Time: 下午4:14
 * To change this template use File | Settings | File Templates.
 */
public interface OrderDao extends CollectiveAggregateRootDao<Order, UserShardingId> {

}
