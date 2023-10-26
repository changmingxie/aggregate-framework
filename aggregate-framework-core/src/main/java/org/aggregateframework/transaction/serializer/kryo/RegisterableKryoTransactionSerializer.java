package org.aggregateframework.transaction.serializer.kryo;

import com.google.common.collect.Lists;
import org.aggregateframework.transaction.Participant;
import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.transaction.serializer.TransactionSerializer;
import org.aggregateframework.xid.TransactionXid;
import org.aggregateframework.utils.CollectionUtils;

import java.util.List;

/**
 * Created by changming.xie on 9/18/17.
 */
public class RegisterableKryoTransactionSerializer extends RegisterableKryoSerializer<Transaction> implements TransactionSerializer {

    static List<Class> transactionClasses = Lists.newArrayList(
            Transaction.class,
            TransactionXid.class,
            Participant.class);

    public RegisterableKryoTransactionSerializer() {
        this(transactionClasses);
    }

    public RegisterableKryoTransactionSerializer(int initPoolSize) {
        this(initPoolSize, transactionClasses);
    }

    public RegisterableKryoTransactionSerializer(List<Class> registerClasses) {
        super(CollectionUtils.merge(transactionClasses, registerClasses));
    }

    public RegisterableKryoTransactionSerializer(int initPoolSize, List<Class> registerClasses) {
        super(initPoolSize, CollectionUtils.merge(transactionClasses, registerClasses));
    }

    public RegisterableKryoTransactionSerializer(int initPoolSize, List<Class> registerClasses, boolean warnUnregisteredClasses) {
        super(initPoolSize, CollectionUtils.merge(transactionClasses, registerClasses), warnUnregisteredClasses);
    }
}
