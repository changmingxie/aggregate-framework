package org.aggregateframework.test.dao;

import org.aggregateframework.test.command.domain.entity.Payment;
import org.aggregateframework.dao.DomainObjectDao;

/**
 * User: changming.xie
 * Date: 14-6-20
 * Time: 下午6:57
 */
public interface PaymentDao extends DomainObjectDao<Payment, Integer> {
}
