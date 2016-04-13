package org.aggregateframework.test.hierarchicalmodel.command.domain.entity;

/**
 * Created by changming.xie on 3/30/16.
 */
public class DeliveryOrderInfo extends OrderInfo {

    private String deliveryInfo;

    public DeliveryOrderInfo() {
        this.setDtype("DeliveryOrderInfo");
    }

    public String getDeliveryInfo() {
        return deliveryInfo;
    }

    public void setDeliveryInfo(String deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }
}
