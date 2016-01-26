package org.aggregateframework.test.dao;

import org.aggregateframework.dao.CollectiveDomainObjectDao;
import org.aggregateframework.dao.DomainObjectDao;
import org.aggregateframework.test.command.domain.entity.SeatAvailability;
import org.aggregateframework.test.command.domain.entity.UserShardingId;

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
