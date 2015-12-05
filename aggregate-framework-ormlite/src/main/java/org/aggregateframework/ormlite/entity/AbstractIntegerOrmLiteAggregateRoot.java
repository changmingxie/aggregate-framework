package org.aggregateframework.ormlite.entity;

import com.j256.ormlite.field.DatabaseField;
import org.aggregateframework.entity.AbstractAggregateRoot;
import org.aggregateframework.entity.Transient;
import org.aggregateframework.eventhandling.EventContainer;

import java.util.Date;

/**
 * Created by changming.xie on 2014/7/27.
 */
public class AbstractIntegerOrmLiteAggregateRoot extends AbstractAggregateRoot<Integer> {

    @DatabaseField(columnName = "ID", generatedId = true, allowGeneratedIdInsert = true)
    private Integer id;

    @DatabaseField(columnName = "VERSION")
    private Long version = 1L;

    @DatabaseField(columnName = "CREATE_TIME")
    private Date createTime;
    @DatabaseField(columnName = "LAST_UPDATE_TIME")
    private Date lastUpdateTime;


    @Transient
    private transient EventContainer domainEventContainer = new EventContainer();

    @Transient
    private transient EventContainer applicationEventContainer = new EventContainer();

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
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
