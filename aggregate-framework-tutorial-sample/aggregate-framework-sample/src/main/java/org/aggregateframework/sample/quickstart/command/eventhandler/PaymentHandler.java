package org.aggregateframework.sample.quickstart.command.eventhandler;

import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.annotation.TransactionCheck;
import org.aggregateframework.sample.hierarchicalmodel.command.domain.repository.DeliveryOrderRepository;
import org.aggregateframework.sample.quickstart.command.domain.event.PaymentConfirmedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by changming.xie on 4/7/16.
 */
@Service
public class PaymentHandler {

    @Autowired
    DeliveryOrderRepository deliveryOrderRepository;

    AtomicInteger counter = new AtomicInteger();

    @EventHandler(asynchronous = true, postAfterTransaction = true, isTransactionMessage = false, transactionCheck = @TransactionCheck(checkTransactionStatusMethod = "checkPaymentTransaction"))
    public void handlePaymentConfirmedEvent(PaymentConfirmedEvent event) {

        System.out.println("count:" + counter.incrementAndGet());

//        throw new RuntimeException();
//        LockSupport.parkNanos(1000 * 1000 * 500);
    }

    public boolean checkPaymentTransaction(PaymentConfirmedEvent event) {
        return false;
    }

    public void recoverPaymentConfirmedEvent(PaymentConfirmedEvent event) {
//        System.out.println("count:" + counter.incrementAndGet());
//        LockSupport.parkNanos(1000 * 1000 * 500);
    }
}
