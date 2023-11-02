package org.aggregateframework.spring.factory;

import org.aggregateframework.support.BeanFactory;
import org.aggregateframework.support.FactoryBuilder;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Created by changmingxie on 11/22/15.
 */
public class SpringBeanFactory implements ApplicationContextAware, BeanFactory {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        FactoryBuilder.registerBeanFactory(this);
    }

    @Override
    public <T> boolean isFactoryOf(Class<T> clazz) {
        Map map = this.applicationContext.getBeansOfType(clazz);
        return map.size() > 0;
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return this.applicationContext.getBeansOfType(clazz);
    }

    @Override
    public Class<?> getTargetClass(Object target) {
        return AopUtils.getTargetClass(target);
    }

    @Override
    public <T> T getBean(Class<T> var1) {
        return this.applicationContext.getBean(var1);
    }

    public <T> T getBean(String name, Class<T> var1) {
        return this.applicationContext.getBean(name, var1);
    }
}
