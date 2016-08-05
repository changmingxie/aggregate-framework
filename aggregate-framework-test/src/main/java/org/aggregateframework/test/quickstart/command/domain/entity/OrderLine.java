package org.aggregateframework.test.quickstart.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleDomainObject;

import java.math.BigDecimal;

/**
 * Created by changming.xie on 4/7/16.
 */
public class OrderLine extends AbstractSimpleDomainObject<Long> {
    private static final long serialVersionUID = -7836683982395040080L;

    private int productId;

    private int price;

    private int quantity;

    private Order order;

    public OrderLine() {

    }

    public OrderLine(int productId, int price,int quantity) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    protected void setOrder(Order order) {
        this.order = order;
    }

    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    public BigDecimal getTotalAmount() {
        return BigDecimal.valueOf(quantity * price);
    }

    public int getProductId() {
        return productId;
    }
}
