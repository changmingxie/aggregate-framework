package org.aggregateframework.basic.usage.entity;

import lombok.*;
import org.aggregateframework.entity.AbstractSimpleDomainObject;

/**
 * @author Nervose.Wu
 * @date 2023/6/26 14:15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubAccount extends AbstractSimpleDomainObject<Long> {

    private Long id;

    private String accountId;

    private Account account;

    private Integer eventStatus;
}
