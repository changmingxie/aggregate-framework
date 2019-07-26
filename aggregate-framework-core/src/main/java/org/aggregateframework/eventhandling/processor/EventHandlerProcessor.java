package org.aggregateframework.eventhandling.processor;

import org.aggregateframework.SystemException;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.transaction.EventParticipant;
import org.aggregateframework.eventhandling.transaction.EventTransaction;
import org.aggregateframework.eventhandling.transaction.TransactionMethodInvocation;
import org.aggregateframework.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mengyun.commons.bean.FactoryBuilder;
import org.mengyun.compensable.transaction.Invocation;
import org.mengyun.compensable.transaction.Transaction;
import org.mengyun.compensable.transaction.TransactionType;
import org.mengyun.compensable.transaction.repository.TransactionRepository;

/**
 * Created by changmingxie on 12/2/15.
 */
public class EventHandlerProcessor {

    public static void proceed(EventInvokerEntry eventInvokerEntry) {

        EventHandler eventHandler = ReflectionUtils.getAnnotation(eventInvokerEntry.getMethod(), EventHandler.class);
        if (eventHandler.asynchronous()) {
            AsyncMethodInvoker.getInstance().invoke(eventInvokerEntry);
        } else {
            SyncMethodInvoker.getInstance().invoke(eventInvokerEntry);
        }
    }

    public static void prepare(EventInvokerEntry entry) {

        EventHandler eventHandler = ReflectionUtils.getAnnotation(entry.getMethod(), EventHandler.class);

        if (eventHandler.isTransactionMessage()) {

            if (StringUtils.isEmpty(eventHandler.transactionCheck().checkTransactionStatusMethod())) {

                throw new SystemException("checkTransactionStatusMethod cannot be empty when isTransactionMessage is true");
            }

            String transactionRepositoryName = eventHandler.transactionCheck().compensableTransactionRepository();

            TransactionRepository transactionRepository = FactoryBuilder.factoryOf(TransactionRepository.class).getInstance(transactionRepositoryName);

            Transaction transaction = new EventTransaction(TransactionType.ROOT);

            Invocation invocation = new TransactionMethodInvocation(entry.getTarget().getClass(), entry.getMethod().getName(), eventHandler.transactionCheck().checkTransactionStatusMethod(), entry.getMethod().getParameterTypes(), entry.getParams());
            EventParticipant participant = new EventParticipant(invocation);

            transaction.enlistParticipant(participant);

            //transactionRepository.update(transaction);

            transactionRepository.create(transaction);

            entry.setTransaction(transaction);
        }
    }
}
