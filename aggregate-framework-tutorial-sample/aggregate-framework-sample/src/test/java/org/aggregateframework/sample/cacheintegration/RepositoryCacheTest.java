package org.aggregateframework.sample.cacheintegration;

import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.domain.repository.OrderRepository;
import org.aggregateframework.sample.quickstart.command.domain.repository.PaymentRepository;
import org.aggregateframework.sample.quickstart.command.service.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by changming.xie on 9/30/16.
 */
public class RepositoryCacheTest extends AbstractTestCase implements Serializable {

    private static final long serialVersionUID = -5804819777997595926L;
    @Autowired
    OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Before
    public void before() {
        orderRepository.deleteAll();
        paymentRepository.deleteAll();
    }


    @Test
    public void given_mulitple_threads_when_concurrent_place_order_then_all_success() throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(200);

        List<Future> futures = new ArrayList<Future>();

        for (int i = 0; i < 10; i++) {
            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 1; j++) {
                        PricedOrder pricedOrder = orderService.placeOrder(1, 10, j);
                    }
                }
            });
            futures.add(future);
        }

        for (Future future : futures) {
            future.get();
        }
    }
}