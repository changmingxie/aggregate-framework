package org.aggregateframework.spring.datasource;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.transaction.PlatformTransactionManager;

public class TransactionManagerAutoProxyCreator extends AbstractAutoProxyCreator {
    private static final long serialVersionUID = 7736935531455160187L;

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource) throws BeansException {
        return isMatch(beanClass) ? PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS : DO_NOT_PROXY;
    }

    private boolean isMatch(Class<?> clazz) {
        if (PlatformTransactionManager.class.isAssignableFrom(clazz)
                && !SessionDataSourceTransactionManager.class.isAssignableFrom(clazz)) {
            return true;
        }
        return false;
    }
}
