package org.aggregateframework.sample.hierarchicalmodel.command.domain.entity;

/**
 * Created by changming.xie on 3/30/16.
 */
public class JobOrder extends HierarchicalOrder {

    private String job;

    public JobOrder() {
        this.setDtype("JobOrder");
    }

    public JobOrderInfo getOrderInfo() {
        return (JobOrderInfo) super.getOrderInfo();
    }
}
