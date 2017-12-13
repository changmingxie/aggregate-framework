package org.aggregateframework.ormlite.entity;

import com.j256.ormlite.field.DatabaseField;
import org.aggregateframework.entity.AbstractAggregateRoot;
import org.aggregateframework.entity.Transient;
import org.aggregateframework.entity.EventContainer;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by changming.xie on 2014/7/27.
 */
public class AbstractGenericOrmLiteAggregateRoot<ID extends Serializable> extends AbstractAggregateRoot<ID> {

    private static final long serialVersionUID = -4219108002986899177L;
    @DatabaseField(columnName = "ID", generatedId = true)
    private ID id;

    @DatabaseField(columnName = "VERSION")
    private Long version = 1L;

    @DatabaseField(columnName = "CREATE_TIME")
    private Date createTime;
    @DatabaseField(columnName = "LAST_UPDATE_TIME")
    private Date lastUpdateTime;


    @Transient
    private transient EventContainer domainEventContainer = new EventContainer();
    
    @Override
    public EventContainer getDomainEventContainer() {
        return domainEventContainer;
    }

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


    @Override
    public ID getId() {
        return (ID) id;
    }

    @Override
    public void setId(ID id) {
        this.id = id;
    }
}
