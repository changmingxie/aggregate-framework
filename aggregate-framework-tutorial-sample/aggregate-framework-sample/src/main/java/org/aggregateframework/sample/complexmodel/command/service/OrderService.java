package org.aggregateframework.sample.complexmodel.command.service;

import org.aggregateframework.sample.complexmodel.command.domain.entity.BookingOrder;

/**
 * User: changming.xie
 * Date: 14-6-3
 * Time: 下午3:44
 */
public interface OrderService {

    public void add(BookingOrder bookingOrder);

    public void add2(String as);
}
