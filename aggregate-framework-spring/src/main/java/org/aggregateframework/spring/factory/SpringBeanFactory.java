package org.aggregateframework.spring.factory;

import org.aggregateframework.factory.DaoFactory;
import org.aggregateframework.factory.FactoryBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Created by changmingxie on 11/22/15.
 */
public class SpringBeanFactory implements DaoFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        FactoryBuilder.registerBeanFactory(this);
    }

    @Override
    public boolean isFactoryOf(Class clazz) {
        Map map = this.applicationContext.getBeansOfType(clazz);
        return map.size() > 0;
    }

    public Map getBeansOfType(Class clazz) {
        return this.applicationContext.getBeansOfType(clazz);
    }

    @Override
    public <T> T getBean(Class<T> var1) {
        return this.applicationContext.getBean(var1);
    }

    public <T> T getBean(String name, Class<T> var1) {
        return this.applicationContext.getBean(name, var1);
    }
}
