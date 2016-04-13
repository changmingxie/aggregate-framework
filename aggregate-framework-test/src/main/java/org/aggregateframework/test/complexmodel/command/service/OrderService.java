package org.aggregateframework.test.complexmodel.command.service;

import org.aggregateframework.test.complexmodel.command.domain.entity.BookingOrder;

/**
 * User: changming.xie
 * Date: 14-6-3
 * Time: 下午3:44
 */
public interface OrderService {

    public void add(BookingOrder bookingOrder);

    public void add2(String as);
}
