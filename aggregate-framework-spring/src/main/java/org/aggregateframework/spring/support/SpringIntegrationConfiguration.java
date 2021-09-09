package org.aggregateframework.spring.support;

import org.aggregateframework.spring.datasource.TransactionManagerAutoProxyCreator;
import org.aggregateframework.spring.datasource.TransactionManagerInterceptor;
import org.aggregateframework.spring.eventhandling.AnnotationEventListenerBeanPostProcessor;
import org.aggregateframework.spring.factory.SpringBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringIntegrationConfiguration {

    private final static String TRANSACTION_MANAGER_INTERCEPTOR_BEAN_NAME = "transactionManagerInterceptor";

    @Bean
    public SpringBeanFactory getSpringBeanFactory() {
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

}
