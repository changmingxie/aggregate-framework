package org.aggregateframework.sample.quickstart.command.domain.entity;

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

    private Long pricedOrderId;

    private PricedOrder pricedOrder;

    private Long id;

    public OrderLine() {

    }

    public OrderLine(int productId, int price, int quantity) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    protected void setPricedOrder(PricedOrder pricedOrder) {
        this.pricedOrder = pricedOrder;
        this.pricedOrderId = pricedOrder.getId();
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public BigDecimal getTotalAmount() {
//        return BigDecimal.valueOf(quantity * price);
        return new BigDecimal(quantity).multiply(new BigDecimal(price));
    }

    public int getProductId() {
        return productId;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public PricedOrder getPricedOrder() {
        return pricedOrder;
    }

}
