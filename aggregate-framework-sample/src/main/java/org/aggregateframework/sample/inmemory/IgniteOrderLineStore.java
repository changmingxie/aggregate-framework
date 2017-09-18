package org.aggregateframework.sample.inmemory;

import org.aggregateframework.ignite.store.TransactionalCacheStoreAdapter;
import org.aggregateframework.sample.quickstart.command.domain.entity.OrderLine;
import org.aggregateframework.sample.quickstart.command.infrastructure.dao.OrderLineDao;
import org.aggregateframework.spring.context.SpringObjectFactory;
import org.apache.ignite.cache.affinity.AffinityKey;
import org.apache.ignite.cache.store.CacheStoreSession;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.resources.CacheStoreSessionResource;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by changming.xie on 10/20/16.
 */
public class IgniteOrderLineStore extends TransactionalCacheStoreAdapter<AffinityKey, OrderLine> implements Serializable {

    private static final long serialVersionUID = -7783917689604652511L;
    /**
     * Store session.
     */
    @CacheStoreSessionResource
    private CacheStoreSession ses;

    @Override
    public void loadCache(IgniteBiInClosure<AffinityKey, OrderLine> clo, Object... args) {

        List<OrderLine> orderLines = SpringObjectFactory.getBean(OrderLineDao.class).findAll();

        for (OrderLine orderLine : orderLines) {
            clo.apply(new AffinityKey(orderLine.getId(), orderLine.getPricedOrder().getId()), orderLine);
        }
    }

    @Override
    public void doWrite(Cache.Entry<? extends AffinityKey, ? extends OrderLine> entry) throws CacheWriterException {

        SpringObjectFactory.getBean(OrderLineDao.class).insert(entry.getValue());
    }

    @Override
    public void doDelete(Object o) throws CacheWriterException {

    }


    @Override
    public OrderLine load(AffinityKey aLong) throws CacheLoaderException {
        return null;
    }

}
