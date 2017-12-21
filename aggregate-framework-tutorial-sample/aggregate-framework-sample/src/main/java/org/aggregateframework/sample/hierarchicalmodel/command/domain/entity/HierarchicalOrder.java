package org.aggregateframework.sample.hierarchicalmodel.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleAggregateRoot;

/**
 * Created by changming.xie on 3/30/16.
 */
public class HierarchicalOrder extends AbstractSimpleAggregateRoot<Integer> {

    private static final long serialVersionUID = 2788858215657724845L;
    private String dtype;

    private String content;

    private OrderInfo orderInfo;

    public OrderInfo getOrderInfo() {
        return orderInfo;
    }

    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }
}
