package org.aggregateframework.utils;

import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.exception.SystemException;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class EventHandlerUtils {

    public static boolean isBatchEventHandler(Method method) {

        Class[] classes = method.getParameterTypes();

        if (classes.length != 1) {
            throw new SystemException(String.format("invalid method parameters,must only one parameter, class:%s, method:%s",
                    method.getClass().getName(),
                    method.getName()));
        }

        return Collection.class.isAssignableFrom(classes[0]);
    }

    public static boolean isCollectionOfTypeEqual(Type type, Class targetClass) {

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            if ((parameterizedType.getRawType() instanceof Class)
                    && Collection.class.isAssignableFrom((Class) parameterizedType.getRawType())) {
                for (Type actualType : parameterizedType.getActualTypeArguments()) {
                    if (actualType.equals(targetClass)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isTypeEqual(Type type, Class targetClass) {
        return type.equals(targetClass);
    }

    public static boolean verifyEventHandler(Method method) {

        EventHandler eventHandler = method.getAnnotation(EventHandler.class);

        if (eventHandler != null) {

            Class[] paramClasses = method.getParameterTypes();

            if (paramClasses == null || paramClasses.length > 1) {
                throw new SystemException(String.format("Invalid method parameter count, must only one parameter! Class:%s, method:%s, parameter count:%d",
                        method.getClass().getName(),
                        method.getName(),
                        paramClasses == null ? 0 : paramClasses.length));
            }

            if (Map.class.isAssignableFrom(paramClasses[0])) {
                throw new SystemException(String.format("Invalid method parameter type, should be normal class type, or Collection type! Class:%s, method:%s, parameter type:%s",
                        method.getClass().getName(),
                        method.getName(),
                        paramClasses[0].getName()));
            }

            if (Collection.class.isAssignableFrom(paramClasses[0])) {

                if (eventHandler.asynchronous() && (eventHandler.asyncConfig().maxBatchSize() <= 0
                        || eventHandler.asyncConfig().maxBatchSize() > eventHandler.asyncConfig().ringBufferSize())) {
                    throw new SystemException(String.format("Invalid maxBatchSize parameter value, should be more then 0, and less or equal then %d! Class:%s, method:%s, parameter type:%s",
                            eventHandler.asyncConfig().ringBufferSize(),
                            method.getClass().getName(),
                            method.getName(),
                            paramClasses[0].getName()));
                }
            }

            return true;
        }

        return false;
    }
}
