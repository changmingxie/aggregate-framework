package org.aggregateframework.storage;

import org.aggregateframework.xid.Xid;

import java.util.Date;

public class TransactionStore {
    private long id = -1;
    private String domain;
    private Xid xid;
    private byte[] content;
    private Date createTime = new Date();
    private Date lastUpdateTime = new Date();
    private long version = 0L;
    private int retriedCount = 0;
    private Integer requestId;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Xid getXid() {
        return xid;
    }

    public void setXid(Xid xid) {
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }


    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String simpleDetail() {
        return "{" +
                "id=" + id +
                ", domain='" + domain + '\'' +
                ", xid=" + xid +
                ", createTime=" + createTime +
                ", lastUpdateTime=" + lastUpdateTime +
                ", version=" + version +
                ", retriedCount=" + retriedCount +
                '}';
    }
}
