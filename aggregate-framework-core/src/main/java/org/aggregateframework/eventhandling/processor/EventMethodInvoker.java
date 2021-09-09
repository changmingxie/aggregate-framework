package org.aggregateframework.eventhandling.processor;

import org.aggregateframework.SystemException;
import org.aggregateframework.eventhandling.EventHandlerHook;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.transaction.EventParticipant;
import org.aggregateframework.eventhandling.transaction.EventTransaction;
import org.aggregateframework.eventhandling.transaction.TransactionMethodInvocation;
import org.aggregateframework.factory.FactoryBuilder;
import org.aggregateframework.transaction.Invocation;
import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.transaction.TransactionType;
import org.aggregateframework.transaction.repository.TransactionRepository;
import org.aggregateframework.transaction.support.TransactionConfigurator;
import org.aggregateframework.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Created by changming.xie on 2/4/16.
 */
public class EventMethodInvoker {

    static final Logger logger = LoggerFactory.getLogger(EventMethodInvoker.class);

    private static volatile EventMethodInvoker INSTANCE = null;

    public static EventMethodInvoker getInstance() {

        if (INSTANCE == null) {

            synchronized (AsyncMethodInvoker.class) {

                if (INSTANCE == null) {
                    INSTANCE = new EventMethodInvoker();
                }
            }
        }

        return INSTANCE;
    }

    public static void preInvoke(EventInvokerEntry entry) {

        EventHandler eventHandler = ReflectionUtils.getAnnotation(entry.getMethod(), EventHandler.class);

        if (eventHandler.isTransactionMessage()) {

            if (StringUtils.isEmpty(eventHandler.transactionCheck().checkTransactionStatusMethod())) {

                throw new SystemException("checkTransactionStatusMethod cannot be empty when isTransactionMessage is true");
            }

            if (FactoryBuilder.factoryOf(TransactionConfigurator.class) == null) {
                throw new SystemException("TransactionConfigurator cannot be found. Seems TransactionConfigurator(or its subclass RecoverConfiguration) instance is not correctly injected.");
            }

            TransactionRepository transactionRepository = FactoryBuilder.factoryOf(TransactionConfigurator.class).getInstance().getTransactionRepository();

            Transaction transaction = new EventTransaction(TransactionType.ROOT);

            Invocation invocation = new TransactionMethodInvocation(entry.getTarget().getClass(), entry.getMethod().getName(), eventHandler.transactionCheck().checkTransactionStatusMethod(), entry.getMethod().getParameterTypes(), entry.getParams());
            EventParticipant participant = new EventParticipant(invocation);

            transaction.enlistParticipant(participant);

            transactionRepository.create(transaction);

            entry.setTransaction(transaction);
        }
    }

    public void invoke(List<EventInvokerEntry> entries) {

        beforeInvoke(entries);

        doInvoke(entries);

        afterInvoke(entries);

        completeInvoke(entries);

    }

    private void beforeInvoke(List<EventInvokerEntry> entries) {

        for (EventInvokerEntry entry : entries) {
            final Method method = entry.getMethod();
            final Object target = entry.getTarget();
            final Object[] params = entry.getParams();
            try {
                EventHandlerHook.INSTANCE.beforeEventHandler(target, method, params);
            } catch (Exception e) {
                throw new SystemException(e);
            }
        }
    }

    private void doInvoke(List<EventInvokerEntry> entries) {

        Collection aggregateParams = (Collection) entries.get(0).getParams()[0];

        for (int i = 1; i < entries.size(); i++) {
            aggregateParams.addAll((Collection) entries.get(i).getParams()[0]);
        }

        try {
            EventInvokerEntry currentEventInvokerEntry = entries.get(0);
            currentEventInvokerEntry.getMethod().invoke(currentEventInvokerEntry.getTarget(), aggregateParams);
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    private void afterInvoke(List<EventInvokerEntry> entries) {

        for (EventInvokerEntry entry : entries) {

            final Method method = entry.getMethod();
            final Object target = entry.getTarget();
            final Object[] params = entry.getParams();
            try {
                EventHandlerHook.INSTANCE.afterEventHandler(target, method, params, null);
            } catch (Exception e) {
                throw new SystemException(e);
            }
        }
    }

    private void completeInvoke(List<EventInvokerEntry> entries) {

        for (EventInvokerEntry entry : entries) {
            completeInvoke(entry);
        }
    }

    public void invoke(EventInvokerEntry entry) {

        final Method method = entry.getMethod();
        final Object target = entry.getTarget();
        final Object[] params = entry.getParams();

        try {
            try {
                EventHandlerHook.INSTANCE.beforeEventHandler(target, method, params);
                method.invoke(target, params);
            } catch (Exception e) {
                EventHandlerHook.INSTANCE.afterEventHandler(target, method, params, e);
                throw e;
            }

            EventHandlerHook.INSTANCE.afterEventHandler(target, method, params, null);

            if (entry.getTransaction() != null) {

                TransactionRepository transactionRepository = FactoryBuilder.factoryOf(TransactionConfigurator.class).getInstance().getTransactionRepository();

                transactionRepository.delete(entry.getTransaction());
            }

        } catch (Throwable e) {
            throw new SystemException(e);
        }
    }

    public void cancelInvoke(EventInvokerEntry entry) {
        completeInvoke(entry);
    }

    private void completeInvoke(EventInvokerEntry entry) {
        try {
            if (entry.getTransaction() != null) {
                TransactionRepository transactionRepository = FactoryBuilder.factoryOf(TransactionConfigurator.class).getInstance().getTransactionRepository();

                transactionRepository.delete(entry.getTransaction());
            }
        } catch (Exception e) {
            logger.error("remove transaction log failed. Ignore the error and the transaction log will be deleted by transaction recovery schedule job.", e);
        }
    }
}
