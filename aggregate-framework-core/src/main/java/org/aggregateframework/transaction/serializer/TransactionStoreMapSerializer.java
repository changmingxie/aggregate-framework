package org.aggregateframework.transaction.serializer;

import org.aggregateframework.exception.SystemException;
import org.aggregateframework.storage.TransactionStore;
import org.aggregateframework.xid.TransactionXid;
import org.aggregateframework.utils.ByteUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by changming.xie on 9/15/16.
 */
public class TransactionStoreMapSerializer {

    public static final String XID = "XID";
    public static final String RETRIED_COUNT = "RETRIED_COUNT";
    public static final String CREATE_TIME = "CREATE_TIME";
    public static final String LAST_UPDATE_TIME = "LAST_UPDATE_TIME";
    public static final String VERSION = "VERSION";
    public static final String CONTENT = "CONTENT";
    public static final String DOMAIN = "DOMAIN";
    public static final String REQUEST_ID = "REQUEST_ID";

    private TransactionStoreMapSerializer() {
    }

    public static Map<byte[], byte[]> serialize(TransactionStore transactionStore, boolean isCreate) {

        Map<byte[], byte[]> map = new HashMap<>();

        map.put(DOMAIN.getBytes(StandardCharsets.UTF_8), transactionStore.getDomain().getBytes(StandardCharsets.UTF_8));
        map.put(XID.getBytes(StandardCharsets.UTF_8), transactionStore.getXid().toString().getBytes(StandardCharsets.UTF_8));
        map.put(RETRIED_COUNT.getBytes(StandardCharsets.UTF_8), ByteUtils.intToBytes(transactionStore.getRetriedCount()));
        map.put(CREATE_TIME.getBytes(StandardCharsets.UTF_8), DateFormatUtils.format(transactionStore.getCreateTime(), "yyyy-MM-dd HH:mm:ss").getBytes(StandardCharsets.UTF_8));
        map.put(LAST_UPDATE_TIME.getBytes(StandardCharsets.UTF_8), DateFormatUtils.format(transactionStore.getLastUpdateTime(), "yyyy-MM-dd HH:mm:ss").getBytes(StandardCharsets.UTF_8));
        map.put(VERSION.getBytes(StandardCharsets.UTF_8), ByteUtils.longToBytes(transactionStore.getVersion()));
        if (isCreate) {
            map.put(CONTENT.getBytes(StandardCharsets.UTF_8), transactionStore.getContent());
        }
        if (transactionStore.getRequestId() != null) {
            map.put(REQUEST_ID.getBytes(StandardCharsets.UTF_8), ByteUtils.intToBytes(transactionStore.getRequestId()));
        }
        return map;
    }

    public static TransactionStore deserialize(Map<byte[], byte[]> map) {

        Map<String, byte[]> propertyMap = new HashMap<>();

        for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
            propertyMap.put(new String(entry.getKey(), StandardCharsets.UTF_8), entry.getValue());
        }

        TransactionStore transactionStore = new TransactionStore();
        transactionStore.setDomain(new String(propertyMap.get(DOMAIN), StandardCharsets.UTF_8));

        transactionStore.setXid(new TransactionXid(new String(propertyMap.get(XID), StandardCharsets.UTF_8)));

        if (propertyMap.containsKey(REQUEST_ID)) {
            transactionStore.setRequestId(ByteUtils.bytesToInt(propertyMap.get(REQUEST_ID)));
        }

        transactionStore.setRetriedCount(ByteUtils.bytesToInt(propertyMap.get(RETRIED_COUNT)));

        try {
            transactionStore.setCreateTime(DateUtils.parseDate(new String(propertyMap.get(CREATE_TIME), StandardCharsets.UTF_8), "yyyy-MM-dd HH:mm:ss"));
            transactionStore.setLastUpdateTime(DateUtils.parseDate(new String(propertyMap.get(LAST_UPDATE_TIME), StandardCharsets.UTF_8), "yyyy-MM-dd HH:mm:ss"));
        } catch (ParseException e) {
            throw new SystemException(e);
        }

        transactionStore.setVersion(ByteUtils.bytesToLong(propertyMap.get(VERSION)));
        transactionStore.setContent(propertyMap.get(CONTENT));

        return transactionStore;
    }
}
