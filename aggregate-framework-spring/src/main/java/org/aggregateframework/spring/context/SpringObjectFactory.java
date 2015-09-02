package org.aggregateframework.spring.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author changming.xie
 */
public class SpringObjectFactory implements ApplicationContextAware {

    public static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    public static Object getBean(String className) {
        return applicationContext.getBean(className);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> type) {

        T bean = null;

        Map<String, T> map = applicationContext.getBeansOfType(type);
        if (map.size() == 1) {
            // only return the bean if there is exactly one
            bean = (T) map.values().iterator().next();
        }
        return bean;
    }
}
