package org.aggregateframework.sample.performancetest;

import org.aggregateframework.eventhandling.processor.AsyncMethodInvoker;
import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.quickstart.command.domain.entity.Payment;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.domain.factory.PaymentFactory;
import org.aggregateframework.sample.quickstart.command.domain.repository.PaymentRepository;
import org.aggregateframework.sample.quickstart.command.service.OrderService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    Random random = new Random();

    @Test
    public void given_muitple_threads_when_placeOrder_then_all_called_succeed() throws ExecutionException, InterruptedException {

        long startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        List<Future> futures = new ArrayList<Future>();

        for (int i = 0; i < 5; i++) {

            int finalI = i;
            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    PricedOrder pricedOrder = orderService.placeOrder(1, 10, finalI);
                }
            });

            futures.add(future);
        }

        for (Future future : futures) {
            future.get();
        }

        AsyncMethodInvoker.getInstance().shutdown();

        System.out.println(String.format("total time:%s", System.currentTimeMillis() - startTime));
    }

    @Test
    public void given_mulitple_threads_when_save_payments_then_all_called_success() throws ExecutionException, InterruptedException {

        long startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future> futures = new ArrayList<Future>();

        for (int i = 0; i < 10; i++) {

            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {

                    long id = random.nextInt();

                    Payment payment = PaymentFactory.buildPayment(id,
                            String.format("p000%s", id), BigDecimal.TEN);

                    paymentRepository.save(payment);
                }
            });

            futures.add(future);
        }

        for (Future future : futures) {
            future.get();
        }

        AsyncMethodInvoker.getInstance().shutdown();

        System.out.println(String.format("total time:%s", System.currentTimeMillis() - startTime));


    }
}
