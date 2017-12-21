package org.aggregateframework.sample.hierarchicalmodel.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleDomainObject;

/**
 * Created by changming.xie on 3/30/16.
 */
public class OrderInfo extends AbstractSimpleDomainObject<Integer> {

    private static final long serialVersionUID = -3404894128686039392L;
    private String name;

    private String dtype;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
