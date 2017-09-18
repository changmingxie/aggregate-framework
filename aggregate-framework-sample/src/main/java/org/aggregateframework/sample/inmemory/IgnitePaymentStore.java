package org.aggregateframework.sample.inmemory;

import org.aggregateframework.sample.quickstart.command.domain.entity.Payment;
import org.aggregateframework.sample.quickstart.command.infrastructure.dao.PaymentDao;
import org.aggregateframework.spring.context.SpringObjectFactory;
import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.ignite.cache.store.CacheStoreSession;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.resources.CacheStoreSessionResource;
import org.springframework.dao.DuplicateKeyException;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by changming.xie on 10/20/16.
 */
public class IgnitePaymentStore extends CacheStoreAdapter<Long, Payment> implements Serializable {

    private static final long serialVersionUID = -4350503597992162260L;
    /**
     * Store session.
     */
    @CacheStoreSessionResource
    private CacheStoreSession ses;

    @Override
    public void loadCache(IgniteBiInClosure<Long, Payment> clo, Object... args) {
        List<Payment> payments = SpringObjectFactory.getBean(PaymentDao.class).findAll();

        for (Payment payment : payments) {
            clo.apply(payment.getId(), payment);
        }

    }

    @Override
    public Payment load(Long aLong) throws CacheLoaderException {
        return null;
    }

    @Override
    public void write(Cache.Entry<? extends Long, ? extends Payment> entry) throws CacheWriterException {

        try {
            SpringObjectFactory.getBean(PaymentDao.class).insert(entry.getValue());
        } catch (DuplicateKeyException exception) {
            SpringObjectFactory.getBean(PaymentDao.class).updateAll(Arrays.asList(entry.getValue()));
        }

    }

    @Override
    public void delete(Object o) throws CacheWriterException {

    }
}
