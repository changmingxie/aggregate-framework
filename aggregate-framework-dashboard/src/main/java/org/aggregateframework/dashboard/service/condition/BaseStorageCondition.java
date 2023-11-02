package org.aggregateframework.dashboard.service.condition;


import org.aggregateframework.dashboard.enums.ConnectionMode;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * @Author huabao.fang
 * @Date 2022/6/9 15:08
 **/
public abstract class BaseStorageCondition implements Condition {


    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        String connectionModeValue = context.getEnvironment().getProperty("spring.agg.dashboard.connection-mode");
        String storageTypeVaule = context.getEnvironment().getProperty("spring.agg.storage.storage-type");

        if (StringUtils.isEmpty(connectionModeValue)) {
            throw new IllegalArgumentException("agg.dashboard.connectionMode is empty");
        }

        ConnectionMode connectionMode = ConnectionMode.nameOf(connectionModeValue.toUpperCase());
        if (connectionMode == null) {
            throw new IllegalArgumentException("agg.dashboard.connectionMode:" + connectionModeValue + " not exist");
        }

        return match(connectionModeValue, storageTypeVaule);
    }

    abstract boolean match(String connectionModeValue, String storageTypeValue);


}
