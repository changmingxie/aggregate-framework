package org.aggregateframework.sample.quickstart.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveAggregateRootDao;
import org.aggregateframework.sample.quickstart.command.domain.entity.Payment;

import java.util.List;

/**
 * Created by changming.xie on 4/8/16.
 */
public interface PaymentDao extends CollectiveAggregateRootDao<Payment, Long> {

    Payment findByPaymentNo(String paymentNo);

    List<Payment> findAll();
}
