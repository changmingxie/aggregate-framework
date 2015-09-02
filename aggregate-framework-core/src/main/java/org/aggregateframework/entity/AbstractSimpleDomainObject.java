package org.aggregateframework.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author changming.xie
 */
public abstract class AbstractSimpleDomainObject<ID extends Serializable> extends AbstractDomainObject<ID> {

    private ID id;

    private Date createTime;

    private Date lastUpdateTime;

    @Override
    public ID getId() {
        return id;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setId(ID id) {
        this.id = id;
    }
}
