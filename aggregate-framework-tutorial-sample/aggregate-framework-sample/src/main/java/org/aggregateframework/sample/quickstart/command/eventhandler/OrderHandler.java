package org.aggregateframework.sample.quickstart.command.eventhandler;

import org.aggregateframework.eventhandling.annotation.AsyncConfig;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.annotation.QueueFullPolicy;
import org.aggregateframework.eventhandling.annotation.TransactionCheck;
import org.aggregateframework.sample.quickstart.command.domain.entity.Payment;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.domain.event.OrderConfirmedEvent;
import org.aggregateframework.sample.quickstart.command.domain.event.OrderPlacedEvent;
import org.aggregateframework.sample.quickstart.command.domain.factory.PaymentFactory;
import org.aggregateframework.sample.quickstart.command.domain.repository.OrderRepository;
import org.aggregateframework.sample.quickstart.command.domain.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by changming.xie on 4/7/16.
 */
@Service
public class OrderHandler {


    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @EventHandler
    public void handleOrderCreatedEvent(List<OrderPlacedEvent> events) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("======begin sync  handler, events，size:" + events.size() + "======");
        stringBuilder.append("\r\n");

        for (OrderPlacedEvent event : events) {
            stringBuilder.append("sync call order no:" + event.getPricedOrder().getMerchantOrderNo());
            stringBuilder.append("\r\n");
        }

        stringBuilder.append("======end sync  handler, events，size:" + events.size() + "======");
        stringBuilder.append("\r\n");
        System.out.println(stringBuilder.toString());
    }

    @EventHandler(asynchronous = false, postAfterTransaction = true, isTransactionMessage = true, transactionCheck = @TransactionCheck(checkTransactionStatusMethod = "checkOrderCreated"))
    public void syncAfterTransactionHandleOrderCreatedEvent(OrderPlacedEvent event) throws InterruptedException {


        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("&&&&&& begin sync single handle &&&&&&");
        stringBuilder.append("\r\n");

        stringBuilder.append("sync single call order no:" + event.getPricedOrder().getMerchantOrderNo());
        stringBuilder.append("\r\n");

        stringBuilder.append("&&&&&& end sync single handle &&&&&&");
        stringBuilder.append("\r\n");

        System.out.println(stringBuilder.toString());

        PricedOrder order = orderRepository.findByMerchantOrderNo(event.getPricedOrder().getMerchantOrderNo());

        Payment payment = PaymentFactory.buildPayment(order.getId(),
                String.format("p000%s", order.getId()), order.getTotalAmount());

        paymentRepository.save(payment);

        System.out.println("first save payment id:" + payment.getId());

        Payment payment2 = PaymentFactory.buildPayment(order.getId(),
                String.format("p000%s", order.getId() + 100000), order.getTotalAmount());

        paymentRepository.saveWithTransactional(payment2);

        System.out.println("second save payment id:" + payment2.getId());

        Thread.sleep(200l);

    }


    @EventHandler(asyncConfig = @AsyncConfig(ringBufferSize = 4096, workPoolSize = 8, queueFullPolicy = QueueFullPolicy.DISCARD), asynchronous = true, postAfterTransaction = true, isTransactionMessage = true, transactionCheck = @TransactionCheck(checkTransactionStatusMethod = "checkOrderCreated"))
    public void asyncBatchHandleOrderCreatedEvent(List<OrderPlacedEvent> events) throws InterruptedException {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("******begin async batch handle, event size:" + events.size() + "******");
        stringBuilder.append("\r\n");

        for (OrderPlacedEvent event : events) {
            stringBuilder.append("async batch call order no:" + event.getPricedOrder().getMerchantOrderNo());
            stringBuilder.append("\r\n");
        }

        stringBuilder.append("******end async batch handle, event size:" + events.size() + "******");
        stringBuilder.append("\r\n");
        System.out.println(stringBuilder.toString());


        for (OrderPlacedEvent event : events) {

            PricedOrder order = orderRepository.findByMerchantOrderNo(event.getPricedOrder().getMerchantOrderNo());

            Payment payment = PaymentFactory.buildPayment(order.getId(),
                    String.format("p000%s", order.getId()), order.getTotalAmount());

            paymentRepository.save(payment);
        }

        Thread.sleep(200l);
    }

    @EventHandler(asyncConfig = @AsyncConfig(ringBufferSize = 4096, workPoolSize = 8, queueFullPolicy = QueueFullPolicy.DISCARD), asynchronous = true, postAfterTransaction = true, isTransactionMessage = true, transactionCheck = @TransactionCheck(checkTransactionStatusMethod = "checkOrderCreated"))
    public void asyncHandleOrderCreatedEvent(OrderPlacedEvent event) throws InterruptedException {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("&&&&&& begin async single handle &&&&&&");
        stringBuilder.append("\r\n");

        stringBuilder.append("async single call order no:" + event.getPricedOrder().getMerchantOrderNo());
        stringBuilder.append("\r\n");

        stringBuilder.append("&&&&&& end async single handle &&&&&&");
        stringBuilder.append("\r\n");

        System.out.println(stringBuilder.toString());

        PricedOrder order = orderRepository.findByMerchantOrderNo(event.getPricedOrder().getMerchantOrderNo());

        Payment payment = PaymentFactory.buildPayment(order.getId(),
                String.format("p000%s", order.getId()), order.getTotalAmount());

        paymentRepository.save(payment);

        Thread.sleep(200l);
    }

    @EventHandler(asynchronous = true, postAfterTransaction = true, isTransactionMessage = true, transactionCheck = @TransactionCheck(checkTransactionStatusMethod = "checkOrderIsConfirmed"))
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        System.out.println("order confirmed event handled");
    }


    @EventHandler(asynchronous = true, postAfterTransaction = true, isTransactionMessage = true, transactionCheck = @TransactionCheck(checkTransactionStatusMethod = "checkOrderIsConfirmed"))
    public void handleOrderConfirmedEvent(List<OrderConfirmedEvent> events) {

        System.out.println("transactional send to mq list,size:" + events.size());
    }

    @EventHandler(asynchronous = true)
    public void handleOrderConfirmedEvent2(List<OrderConfirmedEvent> events) {

        System.out.println("send to mq list,size:" + events.size());
    }

    public boolean checkOrderCreated(OrderPlacedEvent event) {
        return true;
    }

    public boolean checkOrderCreated(List<OrderPlacedEvent> events) {
        return true;
    }

    public boolean checkOrderIsConfirmed(OrderConfirmedEvent event) {
        return true;
    }

    public boolean checkOrderIsConfirmed(List<OrderConfirmedEvent> events) {

        return true;
    }
}