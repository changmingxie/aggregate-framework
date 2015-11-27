package org.aggregateframework.test.command.domainevents;

import org.aggregateframework.test.command.domain.entity.CompositeId;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-10-9
 * Time: 下午3:56
 * To change this template use File | Settings | File Templates.
 */
public class OrderUpdatedEvent {

    private CompositeId id;

    private String content;

    public OrderUpdatedEvent(CompositeId id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public CompositeId getId() {
        return id;
    }
}
