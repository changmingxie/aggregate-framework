package org.aggregateframework.sample.quickstart.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleAggregateRoot;
import org.aggregateframework.entity.DaoAwareQuery;
import org.aggregateframework.sample.quickstart.command.domain.event.OrderConfirmedEvent;
import org.aggregateframework.sample.quickstart.command.domain.event.OrderPlacedEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by changming.xie on 4/7/16.
 */
public class PricedOrder extends AbstractSimpleAggregateRoot<Long> {
    private static final long serialVersionUID = -4983024009689367049L;

    private Long id;

    private String merchantOrderNo;

    private int statusId;

//    private String newField = "2020";

    @DaoAwareQuery(mappedBy = "pricedOrder", select = "findByOrderId")
    private List<OrderLine> orderLines = new ArrayList<OrderLine>();

    public PricedOrder() {

    }

    public PricedOrder(String merchantOrderNo) {
        this.merchantOrderNo = merchantOrderNo;
        this.apply(new OrderPlacedEvent(this));
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
    }

    public void addOrderLine(OrderLine orderLine) {
        this.orderLines.add(orderLine);
        orderLine.setPricedOrder(this);
    }

    public BigDecimal getTotalAmount() {

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderLine line : orderLines) {
            totalAmount = totalAmount.add(line.getTotalAmount());
        }

        return totalAmount;
    }

    public void confirm(int statusId) {
        this.statusId = statusId;
        this.apply(new OrderConfirmedEvent(this));
    }

    public String getMerchantOrderNo() {
        return merchantOrderNo;
    }

    public void setMerchantOrderNo(String merchantOrderNo) {
        this.merchantOrderNo = merchantOrderNo;
    }

    public int getStatusId() {
        return statusId;
    }
//
//    public String getNewField() {
//        return newField;
//    }
//
//    public void setNewField(String newField) {
//        this.newField = newField;
//    }
}
