package org.aggregateframework.transaction.server.model;

/**
 * Created by cheng.zeng on 2016/9/2.
 */
public class Transaction {

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getRetried() {
        return retried;
    }

    public void setRetried(String retried) {
        this.retried = retried;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGlobalTxId() {
        return globalTxId;
    }

    public void setGlobalTxId(String globalTxId) {
        this.globalTxId = globalTxId;
    }

    public String getBranchQualifier() {
        return branchQualifier;
    }

    public void setBranchQualifier(String branchQualifier) {
        this.branchQualifier = branchQualifier;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    private String status;
    private String type;
    private String retried;
    private String createTime;
    private String lastUpdateTime;
    private String content;
    private String globalTxId;
    private String branchQualifier;
    private String key;
    private String domain;

    public void setStatus(String status) {
        switch (status) {
            case "1": {
                this.status = "Trying";
                break;
            }
            case "2": {
                this.status = "Confirming";
                break;
            }
            case "3": {
                this.status = "Cancelling";
                break;
            }
            default:
                this.status = "Unknown";
        }
    }
    
    public void setType(String type) {
        if ("1".equals(type)) {
            this.type = "Root";
        } else {
            this.type = "Branch";
        }
    }
}
