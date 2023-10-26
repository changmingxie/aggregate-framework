package org.aggregateframework.transaction.repository;

import org.aggregateframework.storage.Page;
import org.aggregateframework.storage.StorageRecoverable;
import org.aggregateframework.storage.TransactionStorage;
import org.aggregateframework.storage.TransactionStore;
import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.transaction.serializer.TransactionSerializer;
import org.aggregateframework.xid.Xid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DefaultTransactionRepository implements TransactionRepository {

    private static final String STORAGE_ROW_ID = "CREATE_RESULT_ID";
    private String domain;

    private TransactionStorage transactionStorage;

    private TransactionSerializer serializer;

    public DefaultTransactionRepository(String domain, TransactionSerializer serializer, TransactionStorage transactionStorage) {
        this.transactionStorage = transactionStorage;
        this.serializer = serializer;
        this.domain = domain;
    }

    @Override
    public String getDomain() {
        return this.domain;
    }

    @Override
    public int create(Transaction transaction) {
        transaction.setVersion(1L);
        TransactionStore transactionStore = getTransactionStore(transaction, true);
        int result = this.transactionStorage.create(transactionStore);
        transaction.setRowId(result);
        return result;
    }

    @Override
    public int update(Transaction transaction) {
        transaction.setVersion(transaction.getVersion() + 1);
        transaction.setLastUpdateTime(new Date());
        //ignore useless field to optimize data transfer size
        TransactionStore transactionStore = getTransactionStore(transaction, false);
        return this.transactionStorage.update(transactionStore);
    }

    @Override
    public int delete(Transaction transaction) {
        //ignore useless field to optimize data transfer size
        TransactionStore transactionStore = getTransactionStore(transaction, false);
        return this.transactionStorage.delete(transactionStore);
    }

    @Override
    public Transaction findByXid(Xid xid) {
        TransactionStore transactionStore = this.transactionStorage.findByXid(this.domain, xid);
        if (transactionStore != null) {
            return getTransaction(transactionStore);
        }
        return null;
    }

    @Override
    public boolean supportRecovery() {
        return this.transactionStorage.supportStorageRecoverable();
    }

    @Override
    public Page<Transaction> findAllUnmodifiedSince(Date date, String offset, int pageSize) {
        if (this.transactionStorage.supportStorageRecoverable()) {
            Page<TransactionStore> transactionStorePage = ((StorageRecoverable) this.transactionStorage).findAllUnmodifiedSince(this.domain, date, offset, pageSize);

            return getTransactionPage(transactionStorePage);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        this.transactionStorage.close();
    }

    private Page<Transaction> getTransactionPage(Page<TransactionStore> transactionStorePage) {
        Page<Transaction> page = new Page<>();
        page.setNextOffset(transactionStorePage.getNextOffset());
        page.setAttachment(transactionStorePage.getAttachment());
        page.setData(getTransactions(transactionStorePage.getData()));
        return page;
    }

    private List<Transaction> getTransactions(List<TransactionStore> transactionStores) {
        List<Transaction> transactions = new ArrayList<>();
        for (TransactionStore transactionStore : transactionStores) {
            transactions.add(getTransaction(transactionStore));
        }
        return transactions;
    }

    public Transaction getTransaction(TransactionStore transactionStore) {
        return TransactionConvertor.getTransaction(serializer, transactionStore);
    }

    public TransactionStore getTransactionStore(Transaction transaction, boolean needContent) {
        return TransactionConvertor.getTransactionStore(serializer, this.domain, transaction,needContent);
    }
}
