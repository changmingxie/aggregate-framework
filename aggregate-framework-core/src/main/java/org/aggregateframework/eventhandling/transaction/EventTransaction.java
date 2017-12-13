package org.aggregateframework.eventhandling.transaction;


import org.mengyun.compensable.transaction.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by changmingxie on 10/26/15.
 */
public class EventTransaction implements Transaction<EventParticipant>, Serializable {

    private static final long serialVersionUID = 7291423944314337931L;

    private TransactionXid xid;

    private TransactionStatus status = TransactionStatus.TRYING;

    private TransactionType transactionType = TransactionType.ROOT;

    private volatile int retriedCount = 0;

    private Date createTime = new Date();

    private Date lastUpdateTime = new Date();

    private long version = 1;

    EventParticipant participant = null;

    private Map<String, Object> attachments = new ConcurrentHashMap<String, Object>();

    public EventTransaction() {
    }

    public EventTransaction(TransactionContext transactionContext) {
        this.xid = transactionContext.getXid();
        this.status = TransactionStatus.TRYING;
        this.transactionType = TransactionType.BRANCH;
    }

    public EventTransaction(TransactionType transactionType) {
        this.xid = new TransactionXid();
        this.status = TransactionStatus.TRYING;
        this.transactionType = transactionType;
    }

    @Override
    public void enlistParticipant(EventParticipant participant) {
        this.participant = participant;
    }

    @Override
    public void commit() throws Throwable {
        this.participant.proceed();
    }

    @Override
    public void rollback() throws Throwable {
        this.participant.proceed();
    }

    @Override
    public TransactionXid getXid() {
        return xid;
    }

    public void setXid(TransactionXid xid) {
        this.xid = xid;
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public int getRetriedCount() {
        return retriedCount;
    }

    @Override
    public void setRetriedCount(int retriedCount) {
        this.retriedCount = retriedCount;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }
}
