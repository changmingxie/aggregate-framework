package org.aggregateframework.sample.web.controller;

import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by changming.xie on 11/10/16.
 */
@RestController
public class BenchmarkController {


    @Autowired
    OrderService orderService;

    @RequestMapping("/place/{productId}/{price}")
    public String place(@PathVariable int productId, @PathVariable int price) throws InterruptedException, ExecutionException {
        Long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(200);

        List<Callable<Void>> callables = new ArrayList<>();

        for (int i = 0; i < 1; i++) {

            int finalI = i;
            callables.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    PricedOrder pricedOrder = orderService.placeOrder(productId, price, finalI);
//                    return orderService.find(pricedOrder.getId());
                    return null;
                }
            });
        }

        List<Future<Void>> futures = executorService.invokeAll(callables);

        for (Future future : futures) {
            future.get();
        }

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

    @RequestMapping("/find/{orderId}")
    public String find(@PathVariable long orderId) {
        Long startTime = System.currentTimeMillis();
        PricedOrder pricedOrder = orderService.find(orderId);
        return pricedOrder == null ? "not found" : pricedOrder.getMerchantOrderNo();
    }
}
