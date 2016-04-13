package org.aggregateframework.test.quickstart.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveDomainObjectDao;
import org.aggregateframework.test.quickstart.command.domain.entity.Payment;

/**
 * Created by changming.xie on 4/8/16.
 */
public interface PaymentDao extends CollectiveDomainObjectDao<Payment, Long> {
    Payment findByPaymentNo(String paymentNo);
}
