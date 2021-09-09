package org.aggregateframework.transaction.serializer;

import org.aggregateframework.serializer.KryoPoolSerializer;
import org.aggregateframework.transaction.Transaction;

/**
 * Created by changming.xie on 9/18/17.
 */
public class KryoTransactionSerializer extends KryoPoolSerializer<Transaction> implements TransactionSerializer {

}
