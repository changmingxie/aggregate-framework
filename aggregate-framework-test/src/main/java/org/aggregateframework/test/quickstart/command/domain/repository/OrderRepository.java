package org.aggregateframework.test.quickstart.command.domain.repository;

import org.aggregateframework.spring.repository.DaoAwareAggregateRepository;
import org.aggregateframework.test.quickstart.command.domain.entity.Order;
import org.springframework.stereotype.Repository;

/**
 * Created by changming.xie on 4/7/16.
 */
@Repository
public class OrderRepository extends DaoAwareAggregateRepository<Order, Long> {

    protected OrderRepository(Class<Order> aggregateType) {
        super(aggregateType);
    }

    public OrderRepository() {
        this(Order.class);
    }

    public Order findByMerchantOrderNo() {
        return null;
    }
}
