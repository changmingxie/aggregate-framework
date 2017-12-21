package org.aggregateframework.sample.complexmodel.command.domain.event;

import org.aggregateframework.sample.complexmodel.command.domain.entity.UserShardingId;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-10-9
 * Time: 下午3:56
 * To change this template use File | Settings | File Templates.
 */
public class OrderUpdatedEvent {

    private UserShardingId id;

    private String content;

    public OrderUpdatedEvent(UserShardingId id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public UserShardingId getId() {
        return id;
    }
}
