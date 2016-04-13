package org.aggregateframework.test.hierarchicalmodel.command.domain.repository;

import org.aggregateframework.spring.repository.DaoAwareAggregateRepository;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.DeliveryOrder;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.JobOrder;

/**
 * Created by changming.xie on 3/30/16.
 */
public class JobOrderRepository extends DaoAwareAggregateRepository<JobOrder, Integer> {
    protected JobOrderRepository(Class<JobOrder> aggregateType) {
        super(aggregateType);
    }

    public JobOrderRepository() {
        this(JobOrder.class);
    }
}
