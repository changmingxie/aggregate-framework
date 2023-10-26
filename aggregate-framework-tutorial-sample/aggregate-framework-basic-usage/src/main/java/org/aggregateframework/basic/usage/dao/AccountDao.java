package org.aggregateframework.basic.usage.dao;


import org.aggregateframework.basic.usage.entity.Account;
import org.aggregateframework.dao.AggregateRootDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Nervose.Wu
 * @date 2023/6/26 14:33
 */

public interface AccountDao extends AggregateRootDao<Account,Long> {

    Account findByAccountId(@Param("accountId") String accountId);

    List<Account> findByAccountIds(List<String> accountIds);
}
