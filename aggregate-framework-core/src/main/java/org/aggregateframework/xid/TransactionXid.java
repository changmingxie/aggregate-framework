package org.aggregateframework.xid;


import org.aggregateframework.support.FactoryBuilder;

import java.io.Serializable;

/**
 * Created by changmingxie on 10/26/15.
 */
public class TransactionXid implements Xid, Serializable {

    private int formatId = AUTO;
    private String xid;

    public TransactionXid() {

    }

    public TransactionXid(String xidString) {
        this.xid = xidString;
    }

    public TransactionXid(int formatId, String xidString) {
        this.formatId = formatId;
        this.xid = xidString;
    }

    public static TransactionXid withUniqueIdentity(Object uniqueIdentity) {
        int formatId = AUTO;
        String xid = null;
        if (uniqueIdentity == null) {
            xid = FactoryBuilder.factoryOf(UUIDGenerator.class).getInstance().generate();
        } else {
            xid = uniqueIdentity.toString();
            formatId = CUSTOMIZED;
        }
        return new TransactionXid(formatId, xid);
    }

    public static TransactionXid withUuid() {
        return new TransactionXid(FactoryBuilder.factoryOf(UUIDGenerator.class).getInstance().generate());
    }

    @Override
    public String toString() {
        return this.xid;
    }

    @Override
    public int hashCode() {
        if (this.xid == null) {
            return 0;
        }
        return this.xid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        TransactionXid other = (TransactionXid) obj;
        return this.xid.equals(other.xid);
    }

    @Override
    public int getFormatId() {
        return this.formatId;
    }

    @Override
    public String getXid() {
        return xid;
    }

}


