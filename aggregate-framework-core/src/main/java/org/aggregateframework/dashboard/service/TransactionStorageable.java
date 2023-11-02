package org.aggregateframework.dashboard.service;

import org.aggregateframework.storage.TransactionStorage;

/**
 * @Author huabao.fang
 * @Date 2022/6/17 15:34
 **/
public interface TransactionStorageable {

    TransactionStorage getTransactionStorage();
}
