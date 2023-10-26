package org.aggregateframework.server.service;

import org.aggregateframework.AggServer;
import org.aggregateframework.dashboard.service.impl.BaseDomainServiceImpl;
import org.aggregateframework.storage.TransactionStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author huabao.fang
 * @Date 2022/6/14 12:18
 **/
@Service
public class DomainServiceImpl extends BaseDomainServiceImpl {

    @Autowired
    private AggServer aggServer;


    @Override
    public TransactionStorage getTransactionStorage() {
        return aggServer.getTransactionStorage();
    }
}
