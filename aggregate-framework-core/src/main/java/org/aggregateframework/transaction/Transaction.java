package org.aggregateframework.transaction;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by changming.xie on 8/23/17.
 */
public interface Transaction<T extends Participant> extends Serializable {

    Xid getXid();

    int getRetriedCount();

    void setRetriedCount(int retriedCount);

    Date getCreateTime();

    Date getLastUpdateTime();

    void setLastUpdateTime(Date date);

    long getVersion();

    void setVersion(long currentVersion);

    void setStatus(TransactionStatus status);

    void commit() throws Throwable;

    void rollback() throws Throwable;

    void enlistParticipant(T participant);

    TransactionType getTransactionType();

    TransactionStatus getStatus();
}
