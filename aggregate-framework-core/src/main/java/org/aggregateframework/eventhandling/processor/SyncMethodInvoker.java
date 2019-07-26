package org.aggregateframework.eventhandling.processor;

import org.aggregateframework.SystemException;
import org.aggregateframework.eventhandling.EventHandlerHook;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.mengyun.commons.bean.FactoryBuilder;
import org.mengyun.compensable.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by changming.xie on 2/4/16.
 */
public class SyncMethodInvoker {

    static final Logger logger = LoggerFactory.getLogger(SyncMethodInvoker.class);

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

            long start = System.currentTimeMillis();

            try {
                EventHandlerHook.INSTANCE.beforeEventHandler(target, method, params);
                method.invoke(target, params);
            } catch (Exception e) {
                EventHandlerHook.INSTANCE.afterEventHandler(target, method, params, e);
                throw e;
            }

            long end = System.currentTimeMillis();
            if (end - start > 100) {
                logger.info("EventHandler [{}.{}] called use: {} ms",
                        target.getClass().getSimpleName(),
                        method.getName(),
                        (end - start));
            }

            EventHandlerHook.INSTANCE.afterEventHandler(target, method, params, null);

            if (eventInvokerEntry.getTransaction() != null) {

                EventHandler eventHandler = method.getAnnotation(EventHandler.class);

                TransactionRepository transactionRepository = FactoryBuilder.factoryOf(TransactionRepository.class).getInstance(eventHandler.transactionCheck().compensableTransactionRepository());

                transactionRepository.delete(eventInvokerEntry.getTransaction());
            }

        } catch (Throwable e) {
            throw new SystemException(e);
        }
    }
}
