package org.aggregateframework.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * User: changming.xie
 * Date: 14-6-25
 * Time: 下午6:57
 */
public interface DomainObject<ID extends Serializable> extends Serializable {

    ID getId();

    boolean isNew();

    Date getCreateTime();

    Date getLastUpdateTime();
}
