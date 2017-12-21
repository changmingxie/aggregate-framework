package org.aggregateframework.sample.inmemory;

import org.aggregateframework.OptimisticLockException;
import org.aggregateframework.ignite.store.TransactionalCacheStoreAdapter;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.infrastructure.dao.OrderDao;
import org.aggregateframework.utils.DomainObjectUtils;
import org.apache.ignite.cache.store.CacheStoreSession;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.resources.CacheStoreSessionResource;
import org.mengyun.commons.bean.FactoryBuilder;
import org.springframework.dao.DuplicateKeyException;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by changming.xie on 10/20/16.
 */
public class IgniteOrderStore extends TransactionalCacheStoreAdapter<Long, PricedOrder> implements Serializable {

    private static final long serialVersionUID = -2472099236121977641L;

    /**
     * Store session.
     */
    @CacheStoreSessionResource
    private CacheStoreSession ses;


    public IgniteOrderStore() {

    }

    @Override
    public void loadCache(IgniteBiInClosure<Long, PricedOrder> clo, Object... args) {

        List<PricedOrder> pricedOrders = FactoryBuilder.factoryOf(OrderDao.class).getInstance().findAll();

        for (PricedOrder pricedOrder : pricedOrders) {
            clo.apply(pricedOrder.getId(), pricedOrder);
        }
    }

    @Override
    public PricedOrder load(Long aLong) throws CacheLoaderException {

        return null;
    }

    @Override
    public void doWrite(Cache.Entry<? extends Long, ? extends PricedOrder> entry) throws CacheWriterException {

        try {
            int effectedCount = FactoryBuilder.factoryOf(OrderDao.class).getInstance().insert(entry.getValue());
        } catch (DuplicateKeyException e) {
            int effectedCount = FactoryBuilder.factoryOf(OrderDao.class).getInstance().updateAll(Arrays.asList(entry.getValue()));

            if (effectedCount < 1) {
                throw new OptimisticLockException();
            }

            DomainObjectUtils.setField(entry.getValue(), DomainObjectUtils.VERSION, entry.getValue().getVersion() + 1);
        }
    }

    @Override
    public void doWriteAll(Collection<Cache.Entry<? extends Long, ? extends PricedOrder>> entries) {
//        orderDao.insertAll(entries)

        Collection<PricedOrder> entities = new ArrayList<PricedOrder>();

        for (Cache.Entry<? extends Long, ? extends PricedOrder> pricedOrder : entries) {
            entities.add(pricedOrder.getValue());
        }

        FactoryBuilder.factoryOf(OrderDao.class).getInstance().insertAll(entities);
    }

    @Override
    public void doDelete(Object o) throws CacheWriterException {

    }
}
