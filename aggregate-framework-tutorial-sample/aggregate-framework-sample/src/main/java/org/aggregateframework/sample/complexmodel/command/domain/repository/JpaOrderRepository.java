package org.aggregateframework.sample.complexmodel.command.domain.repository;

import org.aggregateframework.sample.complexmodel.command.domain.entity.BookingOrder;
import org.aggregateframework.spring.repository.DaoAwareAggregateRepository;
import org.aggregateframework.sample.complexmodel.command.domain.entity.UserShardingId;
import org.springframework.stereotype.Repository;

@Repository
public class JpaOrderRepository extends DaoAwareAggregateRepository<BookingOrder, UserShardingId> {

    public JpaOrderRepository() {
        this(BookingOrder.class);
    }

    protected JpaOrderRepository(Class<BookingOrder> aggregateType) {
        super(aggregateType);
    }
}
