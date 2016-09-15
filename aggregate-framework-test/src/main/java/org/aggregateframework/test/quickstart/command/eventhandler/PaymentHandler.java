package org.aggregateframework.test.quickstart.command.eventhandler;

import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.DeliveryOrder;
import org.aggregateframework.test.hierarchicalmodel.command.domain.entity.DeliveryOrderInfo;
import org.aggregateframework.test.hierarchicalmodel.command.domain.repository.DeliveryOrderRepository;
import org.aggregateframework.test.quickstart.command.domain.event.PaymentConfirmedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by changming.xie on 4/7/16.
 */
@Service
public class PaymentHandler {

    @Autowired
    DeliveryOrderRepository deliveryOrderRepository;

    @EventHandler(asynchronous = true, postAfterTransaction = true, backOffMethod = "backOffMethod")
    public void handlePaymentConfirmedEvent(PaymentConfirmedEvent event) {

        DeliveryOrder deliveryOrder = buildOrder();
        deliveryOrderRepository.save(deliveryOrder);
    }

    public void backOffMethod(PaymentConfirmedEvent event) {
        System.out.println("backOff:" + event.getOrderId());
    }

    private DeliveryOrder buildOrder() {
        DeliveryOrder deliveryOrder = new DeliveryOrder();

        deliveryOrder.setOrderInfo(new DeliveryOrderInfo());

        deliveryOrder.getOrderInfo().setDeliveryInfo("deliverinfo");

        return deliveryOrder;
    }
}
