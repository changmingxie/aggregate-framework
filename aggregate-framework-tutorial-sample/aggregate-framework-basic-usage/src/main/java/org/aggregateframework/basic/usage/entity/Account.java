package org.aggregateframework.basic.usage.entity;


import lombok.*;
import org.aggregateframework.basic.usage.event.AccountCreateEvent;
import org.aggregateframework.entity.AbstractSimpleAggregateRoot;
import org.aggregateframework.entity.DaoAwareQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nervose.Wu
 * @date 2023/6/26 14:11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account extends AbstractSimpleAggregateRoot<Long> {

    private Long id;

    private String accountId;

    private Integer eventStatus;

    @DaoAwareQuery(mappedBy = "account", select = "findByParentId")
    private List<SubAccount> subAccounts = new ArrayList<>();

    public void applyAccountCreateEvent(){
        apply(new AccountCreateEvent(accountId));
    }
}
