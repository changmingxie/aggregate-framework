package org.aggregateframework.eventhandling.transaction;

import org.apache.commons.lang3.StringUtils;
import org.mengyun.commons.bean.FactoryBuilder;
import org.mengyun.compensable.transaction.MethodInvocation;

import java.lang.reflect.Method;

/**
 * Created by changming.xie on 10/26/17.
 */
public class TransactionMethodInvocation extends MethodInvocation {
    private static final long serialVersionUID = -5196077135452842976L;

    private String transactionCheckMethod;

    public TransactionMethodInvocation(Class<? extends Object> targetClass, String methodName, String transactionCheckMethod, Class<?>[] parameterTypes, Object[] params) {

        super(targetClass, methodName, parameterTypes, params);
        this.transactionCheckMethod = transactionCheckMethod;
    }

    @Override
    public Object proceed() throws Throwable {
        if (StringUtils.isNotEmpty(this.transactionCheckMethod)) {
            
            Object target = FactoryBuilder.factoryOf(this.getTargetClass()).getInstance();

            Method method = null;

            method = target.getClass().getMethod(this.transactionCheckMethod, this.getParameterTypes());

            Object result = method.invoke(target, this.getArgs());

            if ((Boolean) result) {
                return super.proceed();
            }
        }
        return null;
    }
}
