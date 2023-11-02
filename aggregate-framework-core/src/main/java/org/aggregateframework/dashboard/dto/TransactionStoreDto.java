package org.aggregateframework.dashboard.dto;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.aggregateframework.xid.Xid;

/**
 * @Author huabao.fang
 * @Date 2022/5/25 14:14
 **/
public class TransactionStoreDto {

    private String domain;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private Xid xid;
    private String content;
    private String createTime;
    private String lastUpdateTime;
    private long version;
    private int retriedCount;

    private String xidString;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Xid getXid() {
        return xid;
    }

    public void setXid(Xid xid) {
        this.xid = xid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public int getRetriedCount() {
        return retriedCount;
    }

    public void setRetriedCount(int retriedCount) {
        this.retriedCount = retriedCount;
    }

    public String getXidString() {
        return xidString;
    }

    public void setXidString(String xidString) {
        this.xidString = xidString;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
