package org.aggregateframework.spring.xml;

import com.xfvape.uid.impl.CachedUidGenerator;
import org.aggregateframework.AggClient;
import org.aggregateframework.spring.datasource.TransactionManagerAutoProxyCreator;
import org.aggregateframework.spring.datasource.TransactionManagerInterceptor;
import org.aggregateframework.spring.eventhandling.AnnotationEventListenerBeanPostProcessor;
import org.aggregateframework.spring.factory.SpringBeanFactory;
import org.aggregateframework.spring.xid.DefaultUUIDGenerator;
import org.aggregateframework.spring.xid.SimpleWorkerIdAssigner;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class SpringIntegrationByRegisterDefinitionParser implements BeanDefinitionParser {

    public static final String SPRING_BEAN_FACTORY_BEAN_NAME = "springBeanFactory";
    public static final String ANNOTATION_EVENT_LISTENER_BEAN_POST_PROCESSOR_BEAN_NAME = "annotationEventListenerBeanPostProcessor";
    public static final String TRANSACTION_MANAGER_INTERCEPTOR_BEAN_NAME = "transactionManagerInterceptor";
    public static final String INTERCEPTOR_NAMES_METHOD_NAME = "interceptorNames";
    public static final String TRANSACTION_MANAGER_AUTO_PROXY_CREATOR_BEAN_NAME = "transactionManagerAutoProxyCreator";
    public static final String UUID_GENERATOR = "uuidGenerator";
    public static final String CACHED_UID_GENERATOR = "cachedUidGenerator";
    public static final String AGG_CLIENT = "aggClient";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {


        if (!parserContext.getRegistry().containsBeanDefinition(SPRING_BEAN_FACTORY_BEAN_NAME)) {
            GenericBeanDefinition springBeanFactoryDef = new GenericBeanDefinition();
            springBeanFactoryDef.setBeanClass(SpringBeanFactory.class);
            parserContext.registerBeanComponent(new BeanComponentDefinition(springBeanFactoryDef, SPRING_BEAN_FACTORY_BEAN_NAME));
        }

        if (!parserContext.getRegistry().containsBeanDefinition(ANNOTATION_EVENT_LISTENER_BEAN_POST_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition annotationEventListenerBeanPostProcessorDef = new RootBeanDefinition();
            annotationEventListenerBeanPostProcessorDef.setBeanClass(AnnotationEventListenerBeanPostProcessor.class);
            parserContext.registerBeanComponent(new BeanComponentDefinition(annotationEventListenerBeanPostProcessorDef, ANNOTATION_EVENT_LISTENER_BEAN_POST_PROCESSOR_BEAN_NAME));
        }

        if (!parserContext.getRegistry().containsBeanDefinition(TRANSACTION_MANAGER_INTERCEPTOR_BEAN_NAME)) {
            RootBeanDefinition transactionManagerInterceptorDef = new RootBeanDefinition();
            transactionManagerInterceptorDef.setBeanClass(TransactionManagerInterceptor.class);
            parserContext.registerBeanComponent(new BeanComponentDefinition(transactionManagerInterceptorDef, TRANSACTION_MANAGER_INTERCEPTOR_BEAN_NAME));
        }

        if (!parserContext.getRegistry().containsBeanDefinition(TRANSACTION_MANAGER_AUTO_PROXY_CREATOR_BEAN_NAME)) {
            RootBeanDefinition transactionManagerAutoProxyCreatorDef = new RootBeanDefinition();
            transactionManagerAutoProxyCreatorDef.setBeanClass(TransactionManagerAutoProxyCreator.class);
            transactionManagerAutoProxyCreatorDef.getPropertyValues().addPropertyValue(INTERCEPTOR_NAMES_METHOD_NAME, new String[]{TRANSACTION_MANAGER_INTERCEPTOR_BEAN_NAME});
            parserContext.registerBeanComponent(new BeanComponentDefinition(transactionManagerAutoProxyCreatorDef, TRANSACTION_MANAGER_AUTO_PROXY_CREATOR_BEAN_NAME));
        }

        if (!parserContext.getRegistry().containsBeanDefinition(UUID_GENERATOR)) {

            int timeBits = 28;
            int workBits = 22;
            int seqBits = 13;

            GenericBeanDefinition cachedUidGeneratorDef = new GenericBeanDefinition();
            cachedUidGeneratorDef.setBeanClass(CachedUidGenerator.class);
            MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
            mutablePropertyValues.addPropertyValue("epochStr", "2022-01-01");
            mutablePropertyValues.addPropertyValue("timeBits", timeBits);
            mutablePropertyValues.addPropertyValue("workerBits", workBits);
            mutablePropertyValues.addPropertyValue("seqBits", seqBits);
            mutablePropertyValues.addPropertyValue("workerIdAssigner", new SimpleWorkerIdAssigner(workBits));
            cachedUidGeneratorDef.setPropertyValues(mutablePropertyValues);
            parserContext.registerBeanComponent(new BeanComponentDefinition(cachedUidGeneratorDef, CACHED_UID_GENERATOR));

            GenericBeanDefinition uuidGeneratorDef = new GenericBeanDefinition();
            uuidGeneratorDef.setBeanClass(DefaultUUIDGenerator.class);
            uuidGeneratorDef.getConstructorArgumentValues().addIndexedArgumentValue(0, new RuntimeBeanReference(CACHED_UID_GENERATOR));
            parserContext.registerBeanComponent(new BeanComponentDefinition(uuidGeneratorDef, UUID_GENERATOR));
        }

        if (!parserContext.getRegistry().containsBeanDefinition(AGG_CLIENT)) {
            GenericBeanDefinition aggClientDef = new GenericBeanDefinition();
            aggClientDef.setBeanClass(AggClient.class);
            aggClientDef.setDependsOn(SPRING_BEAN_FACTORY_BEAN_NAME);
            if (element.hasAttribute("client-config")) {
                aggClientDef.getConstructorArgumentValues().addIndexedArgumentValue(0, new RuntimeBeanReference(element.getAttribute("client-config")));
            } else {
                aggClientDef.getConstructorArgumentValues().addIndexedArgumentValue(0, new ConstructorArgumentValues.ValueHolder(null));
            }
            parserContext.registerBeanComponent(new BeanComponentDefinition(aggClientDef, AGG_CLIENT));
        }

        return null;
    }
}
