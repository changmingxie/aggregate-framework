package org.aggregateframework.ha;

public interface SentinelController {
    
    default boolean degrade() {
        return false;
    }
    
}
