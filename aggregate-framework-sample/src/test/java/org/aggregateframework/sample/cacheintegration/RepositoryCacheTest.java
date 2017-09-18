package org.aggregateframework.sample.cacheintegration;

import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.quickstart.command.domain.entity.Payment;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.domain.repository.OrderRepository;
import org.aggregateframework.sample.quickstart.command.domain.repository.PaymentRepository;
import org.aggregateframework.sample.quickstart.command.service.OrderService;
import org.aggregateframework.spring.cache.RedisL2Cache;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisL2Cache redisL2Cache;

    @Test
    public void testFindAll() throws IOException {
        List<PricedOrder> pricedOrders = orderRepository.findAll();

        List<Payment> payments = paymentRepository.findAll();

        redisL2Cache.remove(payments);

        payments = paymentRepository.findAll();

    }


//    @Test
    public void testPrepare() throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(200);

        List<Future> futures = new ArrayList<Future>();

        for (int i = 0; i < 200; i++) {
            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        PricedOrder pricedOrder = orderService.placeOrder(1, 10);
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