package org.aggregateframework.transaction;

import org.aggregateframework.eventhandling.EventHandlerHook;
import org.aggregateframework.exception.SystemException;
import org.aggregateframework.support.FactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by changming.xie on 8/22/17.
 */
public class Invocation {
    private static final long serialVersionUID = -7969140711432461165L;
    static final Logger logger = LoggerFactory.getLogger(Invocation.class);

    private String transactionCheckMethod;

    private Class targetClass;

    private String methodName;

    private Class[] parameterTypes;

    private Object[] args;

    public Invocation(Class<? extends Object> targetClass, String methodName, String transactionCheckMethod, Class<?>[] parameterTypes, Object[] args) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.targetClass = targetClass;
        this.args = args;
        this.transactionCheckMethod = transactionCheckMethod;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getTransactionCheckMethod() {
        return transactionCheckMethod;
    }

    public void setTransactionCheckMethod(String transactionCheckMethod) {
        this.transactionCheckMethod = transactionCheckMethod;
    }

    public Object proceed() {
        if (StringUtils.isNotEmpty(this.transactionCheckMethod)) {

            Object target = FactoryBuilder.factoryOf(this.getTargetClass()).getInstance();

            Method transactionCheckMethod = null;

            try {
                transactionCheckMethod = target.getClass().getMethod(this.transactionCheckMethod, this.getParameterTypes());
            } catch (NoSuchMethodException e) {
                StringBuilder stringBuilder = buildErrorMessage(target);
                logger.error(stringBuilder.toString(), e);
                throw new SystemException(e);
            }


            try {
                Object result = transactionCheckMethod.invoke(target, this.getArgs());

                if ((Boolean) result) {

                    Object eventHandlerResult = null;
                    try {
                        EventHandlerHook.INSTANCE.beforeEventHandler(this);


                        if (StringUtils.isNotEmpty(this.methodName)) {

                            Method method = null;

                            try {
                                method = target.getClass().getMethod(this.methodName, this.getParameterTypes());

                                eventHandlerResult = method.invoke(target, this.args);
                            } catch (Exception e) {
                                throw new SystemException(e);
                            }
                        }

                    } catch (Exception e) {
                        EventHandlerHook.INSTANCE.afterEventHandler(this, e);
                        throw e;
                    }

                    EventHandlerHook.INSTANCE.afterEventHandler(this, null);

                    return eventHandlerResult;
                }

            } catch (Exception e) {
                throw new SystemException(e);
            }

        }
        return null;
    }

    private StringBuilder buildErrorMessage(Object target) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nAgg-Framwork异常,没有找到下面这个方法\n");
        stringBuilder.append(target.getClass().getSimpleName() + "." + this.transactionCheckMethod);
        stringBuilder.append('(');
        for (Class klass : this.getParameterTypes()) {
            stringBuilder.append(klass.getSimpleName());
            stringBuilder.append(", ");
        }
        int len = stringBuilder.length();
        stringBuilder.delete(len - 2, len);
        stringBuilder.append(")\n");
        stringBuilder.append("可能情况：\n");
        stringBuilder.append("1.该函数没有定义\n");
        stringBuilder.append("2.该函数写成了private,请改成public\n");
        stringBuilder.append("3.该函数参数类型和EventHandler中的参数类型不匹配\n");

        for (Method defMethod : target.getClass().getMethods()) {
            if (defMethod.getName().equals(this.transactionCheckMethod)) {
                stringBuilder.append("在类中找到了名字匹配的函数,其签名如下,请比较参数类型:\n");
                stringBuilder.append(defMethod.toString());
                break;
            }
        }
        stringBuilder.append('\n');
        return stringBuilder;
    }
}
