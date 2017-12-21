package org.aggregateframework.sample.quickstart.command.domain.repository;

import org.aggregateframework.cache.L2Cache;
import org.aggregateframework.sample.quickstart.command.domain.entity.Payment;
import org.aggregateframework.sample.quickstart.command.infrastructure.dao.PaymentDao;
import org.aggregateframework.spring.repository.DaoAwareAggregateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by changming.xie on 4/13/16.
 */
@Repository
public class PaymentRepository extends DaoAwareAggregateRepository<Payment, Long> {

    @Autowired
    PaymentDao paymentDao;

    @Autowired(required = false)
    public void setL2Cacher(L2Cache l2Cache) {
        this.l2Cache = l2Cache;
    }

    public PaymentRepository() {
        this(Payment.class);
    }

    protected PaymentRepository(Class<Payment> aggregateType) {
        super(aggregateType);
    }

    public Payment findByPaymentNo(String paymentNo) {
        return paymentDao.findByPaymentNo(paymentNo);
    }
}
