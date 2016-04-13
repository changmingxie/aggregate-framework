package org.aggregateframework.test.complexmodel.command.domain.repository;

import org.aggregateframework.test.complexmodel.command.domain.entity.BookingOrder;
import org.aggregateframework.spring.repository.DaoAwareAggregateRepository;
import org.aggregateframework.test.complexmodel.command.domain.entity.UserShardingId;
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
