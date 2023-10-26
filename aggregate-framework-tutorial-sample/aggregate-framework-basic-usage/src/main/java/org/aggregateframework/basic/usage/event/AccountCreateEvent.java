package org.aggregateframework.basic.usage.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nervose.Wu
 * @date 2023/6/26 16:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateEvent {

    private String accountId;
}
