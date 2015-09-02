package org.aggregateframework.test.command.domainevents;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-10-9
 * Time: 下午3:56
 * To change this template use File | Settings | File Templates.
 */
public class OrderUpdatedEvent {

    private Integer id;

    private String content;

    public OrderUpdatedEvent(Integer id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public Integer getId() {
        return id;
    }
}
