package org.aggregateframework.sample.web.controller;

import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;

/**
 * Created by changming.xie on 11/10/16.
 */
@Controller
public class BenchmarkController {


    @Autowired
    OrderService orderService;

    @RequestMapping("/place/{productId}/{price}")
    public String place(@PathVariable int productId, @PathVariable int price) {
        Long startTime = System.currentTimeMillis();
        orderService.placeOrder(productId, price);
        System.out.println("place cost time:" + (System.currentTimeMillis() - startTime));
        return "success";
    }


    @RequestMapping("/confirm/{orderId}/{statusId}")
    public String confirm(@PathVariable long orderId, @PathVariable int statusId) {
        Long startTime = System.currentTimeMillis();

        orderService.confirm(orderId, statusId);
        System.out.println("confirm cost time:" + (System.currentTimeMillis() - startTime));
        return "success";
    }
}
