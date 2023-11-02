package org.aggregateframework.spring.support;

import com.xfvape.uid.UidGenerator;
import com.xfvape.uid.impl.CachedUidGenerator;
import org.aggregateframework.AggClient;
import org.aggregateframework.ClientConfig;
import org.aggregateframework.spring.datasource.TransactionManagerAutoProxyCreator;
import org.aggregateframework.spring.datasource.TransactionManagerInterceptor;
import org.aggregateframework.spring.eventhandling.AnnotationEventListenerBeanPostProcessor;
import org.aggregateframework.spring.factory.SpringBeanFactory;
import org.aggregateframework.spring.xid.DefaultUUIDGenerator;
import org.aggregateframework.spring.xid.SimpleWorkerIdAssigner;
import org.aggregateframework.xid.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class SpringIntegrationConfiguration {

    @Autowired(required = false)
    private ClientConfig clientConfig;

    private final static String TRANSACTION_MANAGER_INTERCEPTOR_BEAN_NAME = "transactionManagerInterceptor";

    @Bean
    public SpringBeanFactory springBeanFactory() {
        return new SpringBeanFactory();
    }

    @Bean
    public AnnotationEventListenerBeanPostProcessor getAnnotationEventListenerBeanPostProcessor() {
        return new AnnotationEventListenerBeanPostProcessor();
    }

    @Bean(TRANSACTION_MANAGER_INTERCEPTOR_BEAN_NAME)
    public TransactionManagerInterceptor getTransactionManagerInterceptor() {
        return new TransactionManagerInterceptor();
    }

    @Bean
    public TransactionManagerAutoProxyCreator getTransactionManagerAutoProxyCreator() {
        TransactionManagerAutoProxyCreator transactionManagerAutoProxyCreator = new TransactionManagerAutoProxyCreator();
        transactionManagerAutoProxyCreator.setInterceptorNames(TRANSACTION_MANAGER_INTERCEPTOR_BEAN_NAME);
        return transactionManagerAutoProxyCreator;
    }

    @Bean
    @DependsOn({"springBeanFactory"})
    public AggClient getAggClient() {
        return new AggClient(clientConfig);
    }

    @Bean
    public UidGenerator uidGenerator() {
        int timeBits = 28;
        int workBits = 22;
        int seqBits = 13;
        CachedUidGenerator cachedUidGenerator = new CachedUidGenerator();
        cachedUidGenerator.setEpochStr("2022-01-01");
        cachedUidGenerator.setWorkerBits(workBits);
        cachedUidGenerator.setTimeBits(timeBits);
        cachedUidGenerator.setSeqBits(seqBits);
        cachedUidGenerator.setWorkerIdAssigner(new SimpleWorkerIdAssigner(workBits));
        return cachedUidGenerator;
    }

    @Bean
    public UUIDGenerator uuidGenerator() {
        return new DefaultUUIDGenerator(uidGenerator());
    }

}
