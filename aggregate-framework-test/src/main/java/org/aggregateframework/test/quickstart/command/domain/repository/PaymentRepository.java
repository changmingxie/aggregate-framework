package org.aggregateframework.test.quickstart.command.domain.repository;

import org.aggregateframework.spring.repository.DaoAwareAggregateRepository;
import org.aggregateframework.test.quickstart.command.domain.entity.Payment;
import org.aggregateframework.test.quickstart.command.infrastructure.dao.PaymentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by changming.xie on 4/13/16.
 */
@Repository
public class PaymentRepository extends DaoAwareAggregateRepository<Payment, Long> {

    @Autowired
    PaymentDao paymentDao;

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
