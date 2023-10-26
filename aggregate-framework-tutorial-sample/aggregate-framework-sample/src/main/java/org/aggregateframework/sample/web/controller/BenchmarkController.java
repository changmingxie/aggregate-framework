package org.aggregateframework.sample.web.controller;

import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

/**
 * Created by changming.xie on 11/10/16.
 */
@RestController
public class BenchmarkController {


    @Autowired
    private OrderService orderService;

    @GetMapping("/place/{productId}/{price}")
    public String place(@PathVariable int productId, @PathVariable int price) {
        long startTime = System.currentTimeMillis();

        orderService.placeOrder(productId, price, 0);

        System.out.println("place cost time:" + (System.currentTimeMillis() - startTime));
        return "success";
    }


    @PostMapping("/confirm/{orderId}/{statusId}")
    public String confirm(@PathVariable long orderId, @PathVariable int statusId) {
        long startTime = System.currentTimeMillis();

        orderService.confirm(orderId, statusId);
        System.out.println("confirm cost time:" + (System.currentTimeMillis() - startTime));
        return "success";
    }

    @GetMapping("/find/{orderId}")
    public String find(@PathVariable long orderId) {
        PricedOrder pricedOrder = orderService.find(orderId);
        return pricedOrder == null ? "not found" : pricedOrder.getMerchantOrderNo();
    }
}
