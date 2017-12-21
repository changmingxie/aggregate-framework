package org.aggregateframework.sample.complexmodel.command.infrastructure.dao;

import org.aggregateframework.dao.CollectiveDomainObjectDao;
import org.aggregateframework.sample.complexmodel.command.domain.entity.BookingPayment;

/**
 * User: changming.xie
 * Date: 14-6-20
 * Time: 下午6:57
 */
public interface BookingPaymentDao extends CollectiveDomainObjectDao<BookingPayment, Integer> {
}
