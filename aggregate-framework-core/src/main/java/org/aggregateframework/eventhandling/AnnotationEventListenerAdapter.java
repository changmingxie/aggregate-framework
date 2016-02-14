package org.aggregateframework.eventhandling;

import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.processor.EventHandlerProcessor;
import org.aggregateframework.utils.ReflectionUtils;
import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.session.LocalSessionFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    @Override
    public void handle(EventMessage event) {

        for (Method method : methods) {

            Class<?>[] classes = method.getParameterTypes();
            if (classes != null && classes.length > 0) {
                for (Class<?> clazz : classes) {
                    if (clazz.equals(event.getPayloadType())) {
                        EventInvokerEntry eventInvokerEntry = new EventInvokerEntry(method, this.target, event.getPayload());
                        handle(eventInvokerEntry);

                        break;
                    }
                }
            }
        }
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


    private void handle(EventInvokerEntry eventInvokerEntry) {
        EventHandler eventHandler = ReflectionUtils.getAnnotation(eventInvokerEntry.getMethod(), EventHandler.class);
//        if (eventHandler == null) {
//            return;
//        }

        if (eventHandler.postAfterTransaction()) {
            LocalSessionFactory.INSTANCE.requireClientSession().addPostInvoker(eventInvokerEntry);
        } else {
            EventHandlerProcessor.proceed(eventInvokerEntry);
        }
    }
}