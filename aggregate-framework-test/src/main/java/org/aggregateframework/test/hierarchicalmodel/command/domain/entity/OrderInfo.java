package org.aggregateframework.test.hierarchicalmodel.command.domain.entity;

import org.aggregateframework.entity.AbstractSimpleDomainObject;

/**
 * Created by changming.xie on 3/30/16.
 */
public class OrderInfo extends AbstractSimpleDomainObject<Integer> {

    private String name;

    private String dtype;

    @Override
    public Integer getId() {
        return (Integer) super.getId();
    }

    @Override
    public void setId(Integer id) {
        super.setId(id);
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
