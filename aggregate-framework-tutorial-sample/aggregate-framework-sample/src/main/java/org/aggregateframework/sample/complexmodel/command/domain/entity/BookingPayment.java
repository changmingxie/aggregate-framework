package org.aggregateframework.sample.complexmodel.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleDomainObject;

import java.math.BigDecimal;

/**
 * User: changming.xie
 * Date: 14-6-20
 * Time: 下午6:46
 */
public class BookingPayment extends AbstractSimpleDomainObject<Integer> {

    private static final long serialVersionUID = 6555609853935658453L;
    private BigDecimal amount;
    private Integer id;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }
}
