package org.aggregateframework.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author changming.xie
 */
public abstract class AbstractSimpleDomainObject<ID extends Serializable> extends AbstractDomainObject<ID> {

    private static final long serialVersionUID = 5891480982236336994L;

    private Date createTime;

    private Date lastUpdateTime;

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }
}
