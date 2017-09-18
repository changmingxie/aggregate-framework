package org.aggregateframework.retry;

import org.aggregateframework.SystemException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by changming.xie on 8/15/16.
 */
public class SimpleRecoveryCallback implements RecoveryCallback<Object> {

    private Object target;

    private String recoverMethodName = null;

    private Class<?>[] paramTypes;

    private Object[] params;

    public SimpleRecoveryCallback(Object target, String recoverMethodName, Class<?>[] paramTypes, Object[] params) {
        this.target = target;
        this.recoverMethodName = recoverMethodName;
        this.paramTypes = paramTypes;
        this.params = params;
    }

    @Override
    public Object recover(RetryContext context) {

        try {

            List<Class<?>> recoverParamTypes = new ArrayList<Class<?>>();
            List<Object> recoverParams = new ArrayList<Object>();

            recoverParamTypes.addAll(Arrays.asList(paramTypes));
            recoverParams.addAll(Arrays.asList(params));

            Class<?>[] recoverParamTypeArray = new Class<?>[recoverParamTypes.size()];

            Method recoverMethod = null;

            try {
                recoverMethod = target.getClass().getMethod(recoverMethodName, recoverParamTypes.toArray(recoverParamTypeArray));
            } catch (NoSuchMethodException e) {
                recoverMethod = null;
            }
            
            if (recoverMethod == null) {
                recoverParamTypes.add(Throwable.class);
                recoverParams.add(context.getLastThrowable());

                recoverParamTypeArray = new Class<?>[recoverParamTypes.size()];
                recoverMethod = target.getClass().getMethod(recoverMethodName, recoverParamTypes.toArray(recoverParamTypeArray));
            }

            return recoverMethod.invoke(target, recoverParams.toArray());

        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }
}
