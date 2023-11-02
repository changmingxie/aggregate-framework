package org.aggregateframework.transaction.serializer.kryo;


import org.aggregateframework.serializer.KryoPoolSerializer;
import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.transaction.serializer.TransactionSerializer;

/**
 * Created by changming.xie on 9/18/17.
 */
public class KryoTransactionSerializer extends KryoPoolSerializer<Transaction> implements TransactionSerializer {

}
