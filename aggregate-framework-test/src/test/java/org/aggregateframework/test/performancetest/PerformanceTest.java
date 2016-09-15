package org.aggregateframework.test.performancetest;

import org.aggregateframework.eventhandling.processor.AsyncMethodInvoker;
import org.aggregateframework.test.AbstractTestCase;
import org.aggregateframework.test.quickstart.command.domain.entity.Order;
import org.aggregateframework.test.quickstart.command.domain.repository.PaymentRepository;
import org.aggregateframework.test.quickstart.command.service.OrderService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by changming.xie on 8/18/16.
 */
public class PerformanceTest extends AbstractTestCase {


    @Autowired
    OrderService orderService;

    @Autowired
    PaymentRepository paymentRepository;

    @Test
    public void performance_test() throws ExecutionException, InterruptedException {

        long startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(200);

        List<Future> futures = new ArrayList<Future>();

        for (int i = 0; i < 200; i++) {

            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Order order = orderService.placeOrder(1, 10);
                }
            });

            futures.add(future);
        }

        for(Future future:futures) {
            future.get();
        }

        AsyncMethodInvoker.getInstance().shutdown();

        System.out.println(String.format("total time:%s", System.currentTimeMillis() - startTime));
    }
}
