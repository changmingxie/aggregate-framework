package org.aggregateframework.sample.hierarchicalmodel.command.domain.repository;

import org.aggregateframework.repository.DaoAwareAggregateRepository;
import org.aggregateframework.sample.hierarchicalmodel.command.domain.entity.DeliveryOrder;
import org.springframework.stereotype.Repository;

/**
 * Created by changming.xie on 3/30/16.
 */
@Repository
public class DeliveryOrderRepository extends DaoAwareAggregateRepository<DeliveryOrder,Integer> {
    protected DeliveryOrderRepository(Class<DeliveryOrder> aggregateType) {
        super(aggregateType);
    }

    public DeliveryOrderRepository() {
        this(DeliveryOrder.class);
    }
}
