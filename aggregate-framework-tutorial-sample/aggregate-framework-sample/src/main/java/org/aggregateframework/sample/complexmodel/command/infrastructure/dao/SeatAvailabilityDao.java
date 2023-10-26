package org.aggregateframework.sample.complexmodel.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveDomainObjectDao;
import org.aggregateframework.sample.complexmodel.command.domain.entity.SeatAvailability;
import org.aggregateframework.sample.complexmodel.command.domain.entity.UserShardingId;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-10-12
 * Time: 下午5:31
 * To change this template use File | Settings | File Templates.
 */
public interface SeatAvailabilityDao extends CollectiveDomainObjectDao<SeatAvailability, Integer> {

    List<SeatAvailability> findByOrderIds(Collection<UserShardingId> orderIds);

    List<SeatAvailability> findByOrderId(UserShardingId orderId);
}
