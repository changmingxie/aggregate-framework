package org.aggregateframework.sample.inmemory;

import org.aggregateframework.ignite.store.IgniteCacheLoader;
import org.aggregateframework.sample.quickstart.command.domain.entity.OrderLine;
import org.aggregateframework.sample.quickstart.command.domain.entity.Payment;
import org.aggregateframework.sample.quickstart.command.domain.entity.PricedOrder;
import org.aggregateframework.sample.quickstart.command.infrastructure.dao.OrderDao;
import org.aggregateframework.sample.quickstart.command.infrastructure.dao.OrderLineDao;
import org.aggregateframework.sample.quickstart.command.infrastructure.dao.PaymentDao;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.affinity.AffinityKey;
import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.configuration.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.cache.configuration.FactoryBuilder;
import java.util.List;

/**
 * Created by changming.xie on 10/20/16.
 */
public class InMemoryCacheLoader implements IgniteCacheLoader {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCacheLoader.class);

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderLineDao orderLineDao;

    @Autowired
    private PaymentDao paymentDao;

    @Override
    public void load(Ignite ignite) {

        IgniteCache cache = ignite.cache("order");

        if (cache == null) {
            loadData(ignite, "order", IgniteOrderStore.class, Long.class, PricedOrder.class);
            loadData(ignite, "orderline", IgniteOrderLineStore.class, AffinityKey.class, OrderLine.class);
            loadData(ignite, "payment", IgnitePaymentStore.class, Long.class, Payment.class);
        }

        logger.info("start load data into ignite...");

        Long startTime = System.currentTimeMillis();
        loadPricedOrder(ignite);
        loadOrderLine(ignite);
        loadPayment(ignite);

        logger.info("load data into ignite finished. cost time:" + (System.currentTimeMillis() - startTime));
    }

    private <I, T> void loadData(Ignite ignite, String cacheName, Class<? extends CacheStore<I, T>> cacheStoreClass, Class idClass, Class<T> entityClass) {

        CacheConfiguration<I, T> cacheCfg = new CacheConfiguration<I, T>(cacheName);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        cacheCfg.setIndexedTypes(idClass, entityClass);
        cacheCfg.setStartSize(250000);
        cacheCfg.setCacheMode(CacheMode.PARTITIONED);
        cacheCfg.setBackups(0);
        cacheCfg.setOffHeapMaxMemory(0);
        cacheCfg.setSwapEnabled(false);
//        cacheCfg.setCopyOnRead(false);

//        cacheCfg.setCacheStoreFactory(new FactoryBuilder.SingletonFactory(cacheStore));
        cacheCfg.setCacheStoreFactory(FactoryBuilder.factoryOf(cacheStoreClass));


        cacheCfg.setReadThrough(false);
        cacheCfg.setWriteThrough(true);

        IgniteCache<I, T> cache = ignite.getOrCreateCache(cacheCfg);

//        cache.loadCache(null, null);
    }

    private void loadPricedOrder(Ignite ignite) {

        IgniteDataStreamer<Long, PricedOrder> stmr = ignite.dataStreamer("order");

        List<PricedOrder> pricedOrders = orderDao.findAll();

        for (PricedOrder pricedOrder : pricedOrders) {
            stmr.addData(pricedOrder.getId(), pricedOrder);
        }

        stmr.close();
    }

    private void loadOrderLine(Ignite ignite) {

        IgniteDataStreamer<AffinityKey<Long>, OrderLine> stmr = ignite.dataStreamer("orderline");

        List<OrderLine> orderLines = orderLineDao.findAll();


        for (OrderLine orderLine : orderLines) {
            AffinityKey<Long> affinityKey = new AffinityKey(orderLine.getId(), orderLine.getPricedOrder().getId());
            stmr.addData(affinityKey, orderLine);
        }

        stmr.close();
    }

    private void loadPayment(Ignite ignite) {

        IgniteDataStreamer<Long, Payment> stmr = ignite.dataStreamer("payment");

        List<Payment> payments = paymentDao.findAll();

        for (Payment payment : payments) {
            stmr.addData(payment.getId(), payment);
        }

        stmr.close();
    }
}
