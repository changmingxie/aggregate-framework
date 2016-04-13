package org.aggregateframework.test.quickstart.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveDomainObjectDao;
import org.aggregateframework.dao.DomainObjectDao;
import org.aggregateframework.test.quickstart.command.domain.entity.Order;
import org.aggregateframework.test.quickstart.command.domain.entity.OrderLine;

import java.util.List;

/**
 * Created by changming.xie on 4/8/16.
 */
public interface OrderLineDao extends CollectiveDomainObjectDao<OrderLine,Long> {

    public List<OrderLine> findByOrderId(Long orderId);
}
