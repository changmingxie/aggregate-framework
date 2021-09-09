package org.aggregateframework.transaction.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.collect.Lists;
import org.aggregateframework.serializer.RegisterableKryoSerializer;
import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.utils.CollectionUtils;

import java.util.List;

/**
 * Created by changming.xie on 9/18/17.
 */
public class RegisterableKryoTransactionSerializer extends RegisterableKryoSerializer<Transaction> implements TransactionSerializer {

    static List<Class> transactionClasses = Lists.newArrayList(
            org.aggregateframework.eventhandling.transaction.EventTransaction.class,
            org.aggregateframework.eventhandling.transaction.TransactionMethodInvocation.class,
            org.aggregateframework.transaction.TransactionXid.class,
            org.aggregateframework.transaction.TransactionStatus.class,
            org.aggregateframework.eventhandling.transaction.EventParticipant.class,
            org.aggregateframework.transaction.TransactionType.class);

    public RegisterableKryoTransactionSerializer() {
        this(transactionClasses);
    }

    public RegisterableKryoTransactionSerializer(int initPoolSize) {
        this(initPoolSize, Lists.newArrayList(
                org.aggregateframework.eventhandling.transaction.EventTransaction.class,
                org.aggregateframework.eventhandling.transaction.TransactionMethodInvocation.class,
                org.aggregateframework.transaction.TransactionXid.class,
                org.aggregateframework.transaction.TransactionStatus.class,
                org.aggregateframework.eventhandling.transaction.EventParticipant.class,
                org.aggregateframework.transaction.TransactionType.class));
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

    protected void initHook(Kryo kryo) {
        super.initHook(kryo);
    }
}
