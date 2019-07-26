package org.aggregateframework.eventhandling;

import org.aggregateframework.SystemException;
import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * User: changming.xie
 * Date: 14-6-13
 * Time: 下午2:30
 */
public class AnnotationEventListenerAdapter implements SimpleEventListenerProxy {

    private Object target;
    private List<Method> methods = null;

    public AnnotationEventListenerAdapter(Object target) {
        this.target = target;
        this.methods = Collections.unmodifiableList(methodsOf(target.getClass()));
    }

    public static List<Method> methodsOf(Class<?> clazz) {
        List<Method> methods = new LinkedList<Method>();
        Class<?> currentClazz = clazz;

        while (currentClazz != null && !currentClazz.equals(Object.class)) {

            if (!currentClazz.getName().contains(ReflectionUtils.CGLIB_SUB_CLASS_IDENTIFIER)) {
                for (Method method : currentClazz.getDeclaredMethods()) {
                    if (method.getAnnotation(EventHandler.class) != null) {
                        methods.add(method);
                    }
                }
//                methods.addAll(Arrays.asList(currentClazz.getDeclaredMethods()));
                // addMethodsOnDeclaredInterfaces(currentClazz, methods);
            }

            currentClazz = currentClazz.getSuperclass();
        }

        return Collections.unmodifiableList(methods);
    }

    private static void addMethodsOnDeclaredInterfaces(Class<?> currentClazz, List<Method> methods) {
        for (Class iface : currentClazz.getInterfaces()) {
            methods.addAll(Arrays.asList(iface.getDeclaredMethods()));
            addMethodsOnDeclaredInterfaces(iface, methods);
        }
    }

    @Override
    public Class<?> getTargetType() {
        return target.getClass();
    }

    public List<EventInvokerEntry> matchHandler(List<EventMessage> events) {

        Map<Class, List<Object>> eventMap = new LinkedHashMap<Class, List<Object>>();

        for (EventMessage event : events) {

            if (!eventMap.containsKey(event.getPayloadType())) {
                eventMap.put(event.getPayloadType(), new ArrayList<Object>());
            }

            eventMap.get(event.getPayloadType()).add(event.getPayload());
        }

        List<EventInvokerEntry> eventInvokerEntries = new ArrayList<EventInvokerEntry>();

        for (Method method : methods) {

            Type[] types = method.getGenericParameterTypes();

            if (types != null && types.length > 0) {

                for (Type type : types) {

                    for (Map.Entry<Class, List<Object>> entry : eventMap.entrySet()) {

                        if (isTypeEqual(type, entry.getKey())) {

                            if (types.length > 1) {
                                throw new SystemException(String.format("invalid method parameters, class:%s, method:%s", method.getClass().getName(), method.getName()));
                            } else {
                                for (Object param : entry.getValue()) {
                                    EventInvokerEntry eventInvokerEntry = new EventInvokerEntry(entry.getKey(), method, this.target, param);
                                    eventInvokerEntries.add(eventInvokerEntry);
                                }
                            }
                        } else if (isCollectionOfType(type, entry.getKey())) {

                            if (types.length > 1) {
                                throw new SystemException(String.format("invalid method parameters, class:%s, method:%s", method.getClass().getName(), method.getName()));
                            } else {

                                EventInvokerEntry eventInvokerEntry = new EventInvokerEntry(entry.getKey(), method, this.target, entry.getValue());
                                eventInvokerEntries.add(eventInvokerEntry);
                            }
                        }
                        //break;
                    }

                    break;
                }
            }
        }

        return eventInvokerEntries;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += null == this.target ? 0 : this.target.hashCode() * 31;
        return hashCode;
    }

    public boolean equals(Object other) {

        if (null == other) {
            return false;
        }

        if (this == other) {
            return true;
        }

        if (!this.getClass().equals(other.getClass())) {
            return false;
        }

        if (this.target == null) {
            return false;
        }

        AnnotationEventListenerAdapter that = (AnnotationEventListenerAdapter) other;
        return this.target.equals(that.target);
    }

    private boolean isCollectionOfType(Type type, Class targetClass) {

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

    private boolean isTypeEqual(Type type, Class targetClass) {
        return type.equals(targetClass);
    }

}