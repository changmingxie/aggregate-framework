package org.aggregateframework.spring.annotation;

import org.aggregateframework.spring.support.SpringIntegrationConfiguration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class SpringIntegrationConfigurationSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{SpringIntegrationConfiguration.class.getName()};
    }
}
