package org.aggregateframework.transaction.server.dao;

import org.aggregateframework.transaction.server.model.Transaction;

import java.util.List;

/**
 * Created by changming.xie on 9/7/16.
 */

public interface TransactionDao extends AutoCloseable {
    
    String reset_script = "if redis.call('exists',KEYS[1]) == 1 then redis.call('hset',KEYS[1],KEYS[2],unpack(ARGV)); return '1'; end; return '0';";

    List<Transaction> find(Integer pageNum, int pageSize);

    List<Transaction> findDeleted(Integer pageNum, int pageSize);
    
    int count();

    int countDeleted();
    
    @Deprecated
    boolean reset(String globalTxId, String branchQualifier);
    
    void reset(List<String> keys);

    void delete(List<String> keys);

    void restore(List<String> keys);
    
    String getDomain();
    
    @Override
    default void close() throws Exception {
    
    }
    
}

