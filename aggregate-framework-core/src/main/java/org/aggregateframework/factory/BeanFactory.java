package org.aggregateframework.factory;

import java.util.Map;

/**
 * Created by changmingxie on 11/20/15.
 */
public interface BeanFactory {

    <T> T getBean(Class<T> clazz);

    <T> boolean isFactoryOf(Class<T> clazz);

    <T> Map<String, T> getBeansOfType(Class<T> clazz);

    public <T> T getBean(String name, Class<T> clazz);
}
