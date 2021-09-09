package org.aggregateframework.test;

import org.aggregateframework.eventhandling.annotation.EventHandler;

/**
 * Created by Lee on 2020/5/13 15:07.
 */
public class EventHand {
    
    @EventHandler(order = EventHandler.Order.HIGHEST_PRECEDENCE + 1)
    public void sync(TestEvent event) {
    }
    
    @EventHandler
    public void sync1(TestEvent event) {
    }
    
    
    public boolean check(TestEvent event) {
        return true;
    }
    
    
    public static class TestEvent {
    
    }
}
