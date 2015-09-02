package org.aggregateframework.test.command.service;

import org.aggregateframework.test.command.domain.entity.Order;

/**
 * User: changming.xie
 * Date: 14-6-3
 * Time: 下午3:44
 */
public interface OrderService {

    public void add(Order order);

    public void add2(String as);
}
