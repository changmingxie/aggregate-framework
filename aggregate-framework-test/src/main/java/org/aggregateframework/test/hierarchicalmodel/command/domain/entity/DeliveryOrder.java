package org.aggregateframework.test.hierarchicalmodel.command.domain.entity;

/**
 * Created by changming.xie on 3/30/16.
 */
public class DeliveryOrder extends HierarchicalOrder {

    private static final long serialVersionUID = 2259054024797433038L;
    private String deliver;

    public DeliveryOrder() {
        this.setDtype("DeliveryOrder");
    }

    public DeliveryOrderInfo getOrderInfo() {
        return (DeliveryOrderInfo) super.getOrderInfo();
    }
}
