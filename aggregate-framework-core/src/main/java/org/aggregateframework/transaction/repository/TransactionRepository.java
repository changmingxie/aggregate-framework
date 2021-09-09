package org.aggregateframework.transaction.repository;


import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.transaction.TransactionXid;
import org.rocksdb.RocksDBException;

import java.io.Closeable;
import java.util.Date;

/**
 * Created by changmingxie on 11/12/15.
 */
public interface TransactionRepository extends Closeable {

    String getDomain();

    int create(Transaction transaction);

    int update(Transaction transaction);

    int delete(Transaction transaction);

    Transaction findByXid(TransactionXid xid);

    Page<Transaction> findAllUnmodifiedSince(Date date, String offset, int pageSize);
    
    @Override
    default void close() {

    }
    
    default void init() throws RocksDBException {
    
    }
}
