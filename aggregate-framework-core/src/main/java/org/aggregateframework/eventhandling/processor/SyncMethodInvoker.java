package org.aggregateframework.eventhandling.processor;

import org.aggregateframework.SystemException;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.mengyun.commons.bean.FactoryBuilder;
import org.mengyun.compensable.transaction.repository.TransactionRepository;

import java.lang.reflect.Method;

/**
 * Created by changming.xie on 2/4/16.
 */
public class SyncMethodInvoker {

    private static volatile SyncMethodInvoker INSTANCE = null;

    public static SyncMethodInvoker getInstance() {

        if (INSTANCE == null) {

            synchronized (AsyncMethodInvoker.class) {

                if (INSTANCE == null) {
                    INSTANCE = new SyncMethodInvoker();
                }
            }
        }

        return INSTANCE;
    }

    public void invoke(final EventInvokerEntry eventInvokerEntry) {

        final Method method = eventInvokerEntry.getMethod();
        final Object target = eventInvokerEntry.getTarget();
        final Object[] params = eventInvokerEntry.getParams();


        try {

            method.invoke(target, params);

            if (eventInvokerEntry.getTransaction() != null) {

                TransactionRepository transactionRepository = FactoryBuilder.factoryOf(TransactionRepository.class).getInstance();

                transactionRepository.delete(eventInvokerEntry.getTransaction());
            }

        } catch (Throwable e) {
            throw new SystemException(e);
        }
    }
}
