package org.aggregateframework.sample.complexmodel.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveAggregateRootDao;
import org.aggregateframework.sample.complexmodel.command.domain.entity.BookingOrder;
import org.aggregateframework.sample.complexmodel.command.domain.entity.UserShardingId;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-9-16
 * Time: 下午4:14
 * To change this template use File | Settings | File Templates.
 */
public interface BookingOrderDao extends CollectiveAggregateRootDao<BookingOrder, UserShardingId> {

}
