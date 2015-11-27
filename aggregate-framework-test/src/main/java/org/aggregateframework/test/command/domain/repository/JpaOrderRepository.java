package org.aggregateframework.test.command.domain.repository;

import org.aggregateframework.test.command.domain.entity.CompositeId;
import org.aggregateframework.spring.repository.DaoAwareAggregateRepository;
import org.aggregateframework.test.command.domain.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public class JpaOrderRepository extends DaoAwareAggregateRepository<Order, CompositeId> {

    public JpaOrderRepository() {
        this(Order.class);
    }

    protected JpaOrderRepository(Class<Order> aggregateType) {
        super(aggregateType);
    }
}
