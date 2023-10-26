package org.aggregateframework.dashboard.service.impl.local;


import org.aggregateframework.AggClient;
import org.aggregateframework.dashboard.service.condition.LocalStorageCondition;
import org.aggregateframework.dashboard.service.impl.BaseTransactionServiceImpl;
import org.aggregateframework.storage.TransactionStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

/**
 * @Author huabao.fang
 * @Date 2022/6/9 16:27
 **/
@Conditional(LocalStorageCondition.class)
@Service
public class LocalTransactionServiceImpl extends BaseTransactionServiceImpl {

    @Autowired
    private AggClient aggClient;

    @Override
    public TransactionStorage getTransactionStorage() {
        return aggClient.getTransactionStorage();
    }
}
