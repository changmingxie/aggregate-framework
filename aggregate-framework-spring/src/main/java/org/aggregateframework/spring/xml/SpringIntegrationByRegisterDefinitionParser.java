package org.aggregateframework.spring.xml;

import org.aggregateframework.spring.datasource.TransactionManagerAutoProxyCreator;
import org.aggregateframework.spring.datasource.TransactionManagerInterceptor;
import org.aggregateframework.spring.eventhandling.AnnotationEventListenerBeanPostProcessor;
import org.aggregateframework.spring.factory.SpringBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
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

        return null;
    }
}
