package org.aggregateframework.ignite.store;

import org.aggregateframework.ignite.transaction.CacheStoreTransactionManager;
import org.aggregateframework.spring.context.SpringObjectFactory;
import org.apache.ignite.cache.store.CacheStoreAdapter;

import javax.cache.Cache;
import javax.cache.integration.CacheWriterException;
import java.io.Serializable;
import java.util.Collection;

/**
 * Created by changming.xie on 11/16/16.
 */
public abstract class TransactionalCacheStoreAdapter<K, V> extends CacheStoreAdapter<K, V> implements Serializable {

    private static final long serialVersionUID = -6231068383456448100L;

    @Override
    public final void write(Cache.Entry<? extends K, ? extends V> entry) throws CacheWriterException {
        SpringObjectFactory.getBean(CacheStoreTransactionManager.class).ensureBegin();
        doWrite(entry);
    }

    @Override
    public final void writeAll(Collection<Cache.Entry<? extends K, ? extends V>> entries) {
        doWriteAll(entries);
    }

    @Override
    public final void delete(Object key) throws CacheWriterException {
        doDelete(key);
    }

    @Override
    public void sessionEnd(boolean commit) {
        SpringObjectFactory.getBean(CacheStoreTransactionManager.class).commit(commit);
    }

    public void doWriteAll(Collection<Cache.Entry<? extends K, ? extends V>> entries) throws CacheWriterException {
        super.writeAll(entries);
    }

    public abstract void doWrite(Cache.Entry<? extends K, ? extends V> entry) throws CacheWriterException;

    public abstract void doDelete(Object key) throws CacheWriterException;
}
