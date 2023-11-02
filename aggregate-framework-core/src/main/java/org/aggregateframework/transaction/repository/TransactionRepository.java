package org.aggregateframework.transaction.repository;

import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.storage.Page;
import org.aggregateframework.xid.Xid;

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

    Transaction findByXid(Xid xid);

    boolean supportRecovery();

    Page<Transaction> findAllUnmodifiedSince(Date date, String offset, int pageSize);

    @Override
    default void close() {

    }
}
