package org.aggregateframework.transaction;

import org.aggregateframework.SystemException;
import org.aggregateframework.transaction.repository.TransactionRepository;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Created by changmingxie on 10/26/15.
 */
public class TransactionManager {

    static final org.slf4j.Logger logger = LoggerFactory.getLogger(TransactionManager.class.getSimpleName());

    private TransactionRepository transactionRepository;

    private static final ThreadLocal<Deque<Transaction>> CURRENT_TRANSACTION = new ThreadLocal<Deque<Transaction>>();

    private static final ThreadLocal<Participant> CURRENT_PARTICIPANT = new ThreadLocal<Participant>();

    public TransactionManager() {

    }

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionManager(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void begin(Transaction transaction) {
        transactionRepository.create(transaction);
        registerTransaction(transaction);
    }

    public Transaction propagationExistBegin(TransactionContext transactionContext) throws NoExistedTransactionException {
        Transaction transaction = transactionRepository.findByXid(transactionContext.getXid());

        if (transaction != null) {
            transaction.setStatus(TransactionStatus.valueOf(transactionContext.getStatus()));
            registerTransaction(transaction);
            return transaction;
        } else {
            throw new NoExistedTransactionException();
        }
    }

    public void commit() {

        Transaction transaction = getCurrentTransaction();

        transaction.setStatus(TransactionStatus.CONFIRMING);

        transactionRepository.update(transaction);

        try {

            transaction.commit();

            transactionRepository.delete(transaction);

        } catch (Throwable commitException) {
            logger.error("compensable transaction confirm failed.", commitException);
            throw new ConfirmingException(commitException);
        }
    }

    public Transaction getCurrentTransaction() {
        if (isTransactionActive()) {
            return CURRENT_TRANSACTION.get().peek();
        }
        return null;
    }

    public boolean isTransactionActive() {
        Deque<Transaction> transactions = CURRENT_TRANSACTION.get();
        return transactions != null && !transactions.isEmpty();
    }

    public void rollback() {

        Transaction transaction = getCurrentTransaction();
        transaction.setStatus(TransactionStatus.CANCELLING);

        transactionRepository.update(transaction);

        try {
            transaction.rollback();
            transactionRepository.delete(transaction);
        } catch (Throwable rollbackException) {
            logger.error("compensable transaction rollback failed.", rollbackException);
            throw new CancellingException(rollbackException);
        }
    }


    private void registerTransaction(Transaction transaction) {

        if (CURRENT_TRANSACTION.get() == null) {
            CURRENT_TRANSACTION.set(new LinkedList<Transaction>());
        }

        CURRENT_TRANSACTION.get().push(transaction);
    }

    public void cleanAfterCompletion(Transaction transaction) {

        Participant currentParticipant = CURRENT_PARTICIPANT.get();

        if (currentParticipant != null) {
            CURRENT_PARTICIPANT.set(currentParticipant.getParent());
        }

        if (isTransactionActive() && transaction != null) {
            Transaction currentTransaction = getCurrentTransaction();
            if (currentTransaction == transaction) {
                CURRENT_TRANSACTION.get().pop();
            } else {
                throw new SystemException("Illegal transaction when clean after completion");
            }
        }
    }


    public void enlistParticipant(Participant participant) {

        Transaction transaction = this.getCurrentTransaction();

        if (CURRENT_PARTICIPANT.get() == null) {
            transaction.enlistParticipant(participant);
        } else {
            Participant parent = CURRENT_PARTICIPANT.get();
            parent.addChild(participant);
        }

        CURRENT_PARTICIPANT.set(participant);

        transactionRepository.update(transaction);
    }
}