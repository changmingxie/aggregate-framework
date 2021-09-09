package org.aggregateframework.spring.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class AggConfigNamespaceHandlerSupport extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("integration", new SpringIntegrationByRegisterDefinitionParser());
    }
}
