package org.aggregateframework.test.quickstart.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleAggregateRoot;
import org.aggregateframework.spring.entity.DaoAwareQuery;
import org.aggregateframework.test.quickstart.command.domain.event.OrderPlacedEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by changming.xie on 4/7/16.
 */
public class Order extends AbstractSimpleAggregateRoot<Long> {
    private static final long serialVersionUID = -4983024009689367049L;

    private int statusId;

    private String merchantOrderNo;

    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    @DaoAwareQuery(mappedBy = "order", select = "findByOrderId")
    private List<OrderLine> orderLines = new ArrayList<OrderLine>();

    public Order() {

    }

    public Order(String merchantOrderNo) {
        this.merchantOrderNo = merchantOrderNo;
        this.apply(new OrderPlacedEvent(this));
    }

    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
    }

    public void addOrderLine(OrderLine orderLine) {
        this.orderLines.add(orderLine);
        orderLine.setOrder(this);
    }

    public BigDecimal getTotalAmount() {

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderLine line : orderLines) {
            totalAmount = totalAmount.add(line.getTotalAmount());
        }

        return totalAmount;
    }

    public void confirm() {
        this.statusId = 1;
    }

    public String getMerchantOrderNo() {
        return merchantOrderNo;
    }

    public void setMerchantOrderNo(String merchantOrderNo) {
        this.merchantOrderNo = merchantOrderNo;
    }
}
