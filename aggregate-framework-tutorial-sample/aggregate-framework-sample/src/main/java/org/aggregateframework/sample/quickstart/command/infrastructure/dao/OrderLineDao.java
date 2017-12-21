package org.aggregateframework.sample.quickstart.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveDomainObjectDao;
import org.aggregateframework.sample.quickstart.command.domain.entity.OrderLine;

import java.util.List;

/**
 * Created by changming.xie on 4/8/16.
 */
public interface OrderLineDao extends CollectiveDomainObjectDao<OrderLine,Long> {

    public List<OrderLine> findByOrderId(Long orderId);

    public List<OrderLine> findAll();
}
