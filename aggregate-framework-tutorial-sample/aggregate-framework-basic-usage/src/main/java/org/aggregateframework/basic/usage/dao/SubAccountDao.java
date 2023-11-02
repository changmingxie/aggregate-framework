package org.aggregateframework.basic.usage.dao;


import org.aggregateframework.basic.usage.entity.SubAccount;
import org.aggregateframework.dao.DomainObjectDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Nervose.Wu
 * @date 2023/6/26 14:33
 */
public interface SubAccountDao extends DomainObjectDao<SubAccount,Long> {

    List<SubAccount> findByParentId(@Param("parentId") Long parentId);
}
