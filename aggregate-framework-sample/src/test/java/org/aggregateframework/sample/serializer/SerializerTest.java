package org.aggregateframework.sample.serializer;

import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.quickstart.command.domain.entity.Payment;
import org.aggregateframework.sample.quickstart.command.domain.factory.PaymentFactory;
import org.aggregateframework.serializer.KryoPoolSerializer;
import org.aggregateframework.serializer.ObjectSerializer;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by changming.xie on 9/18/17.
 */
public class SerializerTest extends AbstractTestCase {

    private ObjectSerializer objectSerializer = new KryoPoolSerializer();

    @Test
    public void testSerializer() throws ExecutionException, InterruptedException {


        Random random = new Random();

        ExecutorService executorService = Executors.newFixedThreadPool(100);

        List<Future> futures = new ArrayList<Future>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {

            long id = random.nextInt();

            final Payment payment = PaymentFactory.buildPayment(id,
                    String.format("p000%s", id), BigDecimal.TEN);

            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Payment clonedPayment = (Payment) objectSerializer.clone(payment);
                }
            });

            futures.add(future);
        }

        for (Future future : futures) {
            future.get();
        }

        System.out.println(String.format("total time:%s", System.currentTimeMillis() - startTime));

    }

    @Test
    public void testDeque() {
        Deque<Integer> deque = new ArrayDeque<Integer>();
        deque.push(1);
        deque.push(2);
        deque.push(3);

        System.out.println(deque.peek());
        System.out.println(deque.pop());
        System.out.println(deque.peek());
    }
}
