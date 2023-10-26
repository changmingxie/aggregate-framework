package org.aggregateframework.storage;

import io.openmessaging.storage.dledger.entry.DLedgerEntry;
import io.openmessaging.storage.dledger.protocol.AppendEntryRequest;
import io.openmessaging.storage.dledger.protocol.AppendEntryResponse;
import io.openmessaging.storage.dledger.protocol.GetEntriesRequest;
import io.openmessaging.storage.dledger.protocol.GetEntriesResponse;
import io.openmessaging.storage.dledger.proxy.DLedgerProxy;
import org.aggregateframework.storage.domain.DomainStore;
import org.aggregateframework.storage.helper.ShardHolder;
import org.aggregateframework.transaction.serializer.TransactionStoreSerializer;
import org.aggregateframework.xid.Xid;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DLedgerTransactionStorage extends AbstractKVTransactionStorage<DLedgerProxy> {

    private DLedgerProxy dLedgerProxy;

    public DLedgerTransactionStorage(TransactionStoreSerializer serializer, StoreConfig storeConfig) {
        super(serializer, storeConfig);
        dLedgerProxy = storeConfig.getDLedgerProxy();
    }

    @Override
    protected List<TransactionStore> findTransactionsFromOneShard(String domain, DLedgerProxy shard, Set keys) {
        return null;
    }

    @Override
    protected Page findKeysFromOneShard(String domain, DLedgerProxy shard, String currentCursor, int maxFindCount, boolean isMarkDeleted) {
        return null;
    }

    @Override
    protected int count(String domain, DLedgerProxy shard, boolean isMarkDeleted) {
        return 0;
    }

    @Override
    protected ShardHolder<DLedgerProxy> getShardHolder() {
        return null;
    }

    @Override
    protected int doCreate(TransactionStore transactionStore) {


        AppendEntryRequest request = new AppendEntryRequest();
        request.setGroup("g0");
        request.setRemoteId("a0");
        request.setBody(getSerializer().serialize(transactionStore));
        try {
            CompletableFuture<AppendEntryResponse> future = dLedgerProxy.handleAppend(request);
            AppendEntryResponse response = future.get();
            return Long.valueOf(response.getIndex()).intValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int doUpdate(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected int doDelete(TransactionStore transactionStore) {

        transactionStore.getId();
        GetEntriesRequest request = new GetEntriesRequest();
        request.setGroup("g0");
        request.setRemoteId("a0");
        request.setBeginIndex(transactionStore.getId());
        request.setMaxSize(1);
        try {
            CompletableFuture<GetEntriesResponse> response = dLedgerProxy.handleGet(request);
            List<DLedgerEntry> entries = response.get().getEntries();
            return 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int doMarkDeleted(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected int doRestore(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected int doCompletelyDelete(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected TransactionStore doFindOne(String domain, Xid xid, boolean isMarkDeleted) {
        return null;
    }

    @Override
    public void registerDomain(DomainStore domainStore) {

    }

    @Override
    public void updateDomain(DomainStore domainStore) {

    }

    @Override
    public void removeDomain(String domain) {

    }

    @Override
    public DomainStore findDomain(String domain) {
//        dLedgerProxy.handleMetadata();
        return null;
    }

    @Override
    public List<DomainStore> getAllDomains() {
        return null;
    }


    public DLedgerProxy getdLedgerProxy() {
        return dLedgerProxy;
    }

    public void setdLedgerProxy(DLedgerProxy dLedgerProxy) {
        this.dLedgerProxy = dLedgerProxy;
    }
}
