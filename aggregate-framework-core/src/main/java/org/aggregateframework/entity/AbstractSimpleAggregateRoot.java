package org.aggregateframework.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author changming.xie
 */
public abstract class AbstractSimpleAggregateRoot<ID extends Serializable> extends AbstractAggregateRoot<ID> {

    private static final long serialVersionUID = 5687124586118075949L;

    private Long version = 1L;

    private Date createTime;

    private Date lastUpdateTime;
    
    @Override
    public long getVersion() {
        return version;
    }


    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }


}
