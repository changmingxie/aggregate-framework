package org.aggregateframework.transaction;

import org.aggregateframework.factory.FactoryBuilder;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * Created by changmingxie on 11/9/15.
 */
public class MethodInvocation implements Invocation {

    private static final long serialVersionUID = -7969140711432461165L;

    private Class targetClass;

    private String methodName;

    private Class[] parameterTypes;

    private Object[] args;

    public MethodInvocation() {

    }

    public MethodInvocation(Class targetClass, String methodName, Class[] parameterTypes, Object... args) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.targetClass = targetClass;
        this.args = args;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public Object proceed() throws Throwable {
        if (StringUtils.isNotEmpty(this.methodName)) {


            Object target = FactoryBuilder.factoryOf(this.getTargetClass()).getInstance();

            Method method = null;

            method = target.getClass().getMethod(this.methodName, this.getParameterTypes());

            return method.invoke(target, this.args);
        }
        return null;
    }


}
