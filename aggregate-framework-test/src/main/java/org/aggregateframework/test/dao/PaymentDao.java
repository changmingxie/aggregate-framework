package org.aggregateframework.test.dao;

import org.aggregateframework.dao.CollectiveDomainObjectDao;
import org.aggregateframework.test.command.domain.entity.Payment;

/**
 * User: changming.xie
 * Date: 14-6-20
 * Time: 下午6:57
 */
public interface PaymentDao extends CollectiveDomainObjectDao<Payment, Integer> {
}
