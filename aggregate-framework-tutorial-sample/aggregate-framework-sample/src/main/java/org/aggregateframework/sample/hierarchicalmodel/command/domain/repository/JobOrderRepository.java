package org.aggregateframework.sample.hierarchicalmodel.command.domain.repository;

import org.aggregateframework.repository.DaoAwareAggregateRepository;
import org.aggregateframework.sample.hierarchicalmodel.command.domain.entity.JobOrder;

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
