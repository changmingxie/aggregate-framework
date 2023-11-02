package org.aggregateframework.transaction.serializer.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aggregateframework.exception.SystemException;
import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.transaction.serializer.TransactionSerializer;

/**
 * @author Nervose.Wu
 * @date 2022/7/25 10:39
 */
public class FastjsonTransactionSerializer implements TransactionSerializer {

    public FastjsonTransactionSerializer() {
        //for earlier detection
        try {
            Class.forName("com.alibaba.fastjson.JSON");
        } catch (ClassNotFoundException e) {
            throw new SystemException("missing fastjson dependencies");
        }
    }

    @Override
    public byte[] serialize(Transaction transaction) {
        return JSON.toJSONBytes(transaction, SerializerFeature.WriteClassName);
    }

    @Override
    public Transaction deserialize(byte[] bytes) {
        return JSON.parseObject(bytes, Transaction.class, Feature.SupportNonPublicField, Feature.SupportAutoType);
    }

    @Override
    public Transaction clone(Transaction object) {
        return deserialize(serialize(object));
    }
}
