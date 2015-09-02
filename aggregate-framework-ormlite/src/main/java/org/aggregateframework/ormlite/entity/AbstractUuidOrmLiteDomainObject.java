package org.aggregateframework.ormlite.entity;

import org.aggregateframework.entity.AbstractDomainObject;
import com.j256.ormlite.field.DatabaseField;

import java.util.Date;
import java.util.UUID;

/**
 * Created by changming.xie on 2014/7/27.
 */
public class AbstractUuidOrmLiteDomainObject extends AbstractDomainObject<UUID> {

    @DatabaseField(columnName = "ID", generatedId = true,allowGeneratedIdInsert = true)
    private UUID id;

    @DatabaseField(columnName = "CREATE_TIME")
    private Date createTime;

    @DatabaseField(columnName = "LAST_UPDATE_TIME")
    private Date lastUpdateTime;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
