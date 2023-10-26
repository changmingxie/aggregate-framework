package org.aggregateframework.transaction.serializer;

import org.aggregateframework.storage.TransactionStore;
import org.aggregateframework.xid.TransactionXid;
import org.aggregateframework.xid.Xid;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author Nervose.Wu
 * @date 2022/6/22 14:11
 */
public class DefaultTransactionStoreSerializer implements TransactionStoreSerializer {

    private static final int REQUEST_ID_EXIST_FLAG_POS = 1 << 0;

    @Override
    public byte[] serialize(TransactionStore transactionStore) {
        if (transactionStore == null) {
            return new byte[0];
        }

        byte existFlag = 0;
        if (transactionStore.getRequestId() != null) {
            existFlag |= REQUEST_ID_EXIST_FLAG_POS;
        }

        Xid xid = transactionStore.getXid();
        int xidLength = xid.getXid().getBytes().length;

        //domain
        byte[] domainBytes = null;
        int domainLength = 0;
        if (transactionStore.getDomain() != null && transactionStore.getDomain().length() > 0) {
            domainBytes = transactionStore.getDomain().getBytes(StandardCharsets.UTF_8);
            domainLength = domainBytes.length;
        }

        //content
        int contentLength = 0;
        if (transactionStore.getContent() != null && transactionStore.getContent().length > 0) {
            contentLength = transactionStore.getContent().length;
        }

        int totalLen = calTotalLength(domainLength, xidLength, contentLength, transactionStore.getRequestId() != null);

        ByteBuffer byteBuffer = ByteBuffer.allocate(totalLen);
        byteBuffer.put(existFlag);
        //domain
        byteBuffer.putInt(domainLength);
        if (domainLength > 0) {
            byteBuffer.put(domainBytes);
        }
        // TransactionXid xid
        byteBuffer.putInt(xidLength);
        byteBuffer.put(xid.getXid().getBytes());
        //content
        byteBuffer.putInt(contentLength);
        if (contentLength > 0) {
            byteBuffer.put(transactionStore.getContent());
        }
        byteBuffer.putLong(transactionStore.getCreateTime().getTime());
        byteBuffer.putLong(transactionStore.getLastUpdateTime().getTime());
        byteBuffer.putLong(transactionStore.getVersion());
        byteBuffer.putInt(transactionStore.getRetriedCount());
        byteBuffer.putLong(transactionStore.getId());

        if (transactionStore.getRequestId() != null) {
            byteBuffer.putInt(transactionStore.getRequestId());
        }
        return byteBuffer.array();
    }

    @Override
    public TransactionStore deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        TransactionStore transactionStore = new TransactionStore();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byte existFlag = byteBuffer.get();
        //domain
        int domainLength = byteBuffer.getInt();
        if (domainLength > 0) {
            byte[] domainContent = new byte[domainLength];
            byteBuffer.get(domainContent);
            transactionStore.setDomain(new String(domainContent, StandardCharsets.UTF_8));
        }

        // TransactionXid xid
        int xidLength = byteBuffer.getInt();
        byte[] xidContent = new byte[xidLength];
        byteBuffer.get(xidContent);
        TransactionXid xid = new TransactionXid(new String(xidContent, StandardCharsets.UTF_8));
        transactionStore.setXid(xid);

        //byte[] content
        int contentLength = byteBuffer.getInt();
        if (contentLength > 0) {
            byte[] content = new byte[contentLength];
            byteBuffer.get(content);
            transactionStore.setContent(content);
        }
        transactionStore.setCreateTime(new Date(byteBuffer.getLong()));
        transactionStore.setLastUpdateTime(new Date(byteBuffer.getLong()));
        transactionStore.setVersion(byteBuffer.getLong());
        transactionStore.setRetriedCount(byteBuffer.getInt());
        transactionStore.setId(byteBuffer.getLong());
        if ((existFlag & REQUEST_ID_EXIST_FLAG_POS) != 0) {
            transactionStore.setRequestId(byteBuffer.getInt());
        }
        return transactionStore;
    }

    @Override
    public TransactionStore clone(TransactionStore original) {
        if (original == null) {
            return null;
        }
        TransactionStore cloned = new TransactionStore();
        cloned.setVersion(original.getVersion());
        cloned.setXid(original.getXid());
        cloned.setRetriedCount(original.getRetriedCount());
        cloned.setCreateTime(original.getCreateTime());
        cloned.setLastUpdateTime(original.getLastUpdateTime());
        cloned.setContent(original.getContent().clone());
        cloned.setDomain(original.getDomain());
        return cloned;
    }

    private int calTotalLength(int domainLength, int xidLength, int contentLength, boolean existRequestId) {
        // existFlag to distinguish whether optional params existed
        int totalLength = 1
                //String domain
                + 4 + domainLength
                //TransactionXid xid
                + 4 + xidLength
                //byte[] content
                + 4 + contentLength
                //Date createTime
                + 8
                //Date lastUpdateTime
                + 8
                //long version
                + 8
                //int retriedCount
                + 4
                //long id
                + 8;
        if (existRequestId) {
            totalLength = totalLength
                    //Integer requestId
                    + 4;
        }
        return totalLength;
    }
}
