package org.aggregateframework.transaction;

import org.aggregateframework.xid.TransactionXid;
import org.aggregateframework.xid.Xid;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by changming.xie on 8/23/17.
 */
public class Transaction {

    private static final long serialVersionUID = 7291423944314337931L;

    private long rowId = -1;
    private Xid xid;

    private volatile int retriedCount = 0;

    private Date createTime = new Date();

    private Date lastUpdateTime = new Date();

    private long version = 1;

    private Participant participant = null;

    private Map<String, String> attachments = new HashMap<>();

    public Transaction() {

    }

    public Transaction(Xid xid) {
        this.xid = xid;
    }

    public void enlistParticipant(Participant participant) {
        this.participant = participant;
    }

    public void commit() {
        this.participant.proceed();
    }

    public Xid getXid() {
        return xid;
    }

    public void setXid(TransactionXid xid) {
        this.xid = xid;
    }

    public int getRetriedCount() {
        return retriedCount;
    }

    public void setRetriedCount(int retriedCount) {
        this.retriedCount = retriedCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }
}
