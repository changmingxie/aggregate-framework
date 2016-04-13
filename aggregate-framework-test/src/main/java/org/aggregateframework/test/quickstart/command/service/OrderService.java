package org.aggregateframework.test.quickstart.command.service;

import org.aggregateframework.test.quickstart.command.domain.entity.Order;
import org.aggregateframework.test.quickstart.command.domain.factory.OrderFactory;
import org.aggregateframework.test.quickstart.command.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by changming.xie on 4/7/16.
 */
@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    public Order placeOrder(int productId, int price) {
        Order order = OrderFactory.buildOrder(productId, price);
        
        return orderRepository.save(order);
    }
}
