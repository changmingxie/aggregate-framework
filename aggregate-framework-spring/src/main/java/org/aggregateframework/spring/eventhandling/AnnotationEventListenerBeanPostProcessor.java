package org.aggregateframework.spring.eventhandling;

import org.aggregateframework.eventhandling.AnnotationEventListenerAdapter;
import org.aggregateframework.eventbus.EventBus;
import org.aggregateframework.eventhandling.EventListener;
import org.aggregateframework.eventbus.SimpleEventBus;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInfo;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: changming.xie
 * Date: 14-6-13
 * Time: 上午10:46
 */
public class AnnotationEventListenerBeanPostProcessor implements DestructionAwareBeanPostProcessor, ApplicationContextAware {

    private EventBus eventBus = null;
    private ApplicationContext applicationContext;

    public AnnotationEventListenerBeanPostProcessor() {
        eventBus = SimpleEventBus.INSTANCE;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> targetClass = bean.getClass();
        final ClassLoader classLoader = targetClass.getClassLoader();

        if (isPostProcessingCandidate(targetClass)) {
            final EventListener proxy = createEventListenerProxy(bean, true, classLoader);
            subscrible(proxy);
        }

        return bean;
    }

    private void subscrible(EventListener proxy) {
        ensureEventBusInitialized();
        eventBus.subscribe(proxy);
    }

    @SuppressWarnings({"unchecked"})
    private void ensureEventBusInitialized() {
        // if no EventBus is set, find one in the application context
        if (eventBus == null) {
            Map<String, SimpleEventBus> beans = getApplicationContext().getBeansOfType(SimpleEventBus.class);
            if (beans.size() != 1) {
                throw new IllegalStateException(
                        "If no specific EventBus is provided, the application context must "
                                + "contain exactly one bean of type EventBus. The current application context has: "
                                + beans.size());
            } else {
                this.eventBus = beans.entrySet().iterator().next().getValue();
            }
        }
    }

    private EventListener createEventListenerProxy(Object annotatedHandler, boolean proxyTargetClass, ClassLoader classLoader) {

        AnnotationEventListenerAdapter annotationEventListenerAdapter = new AnnotationEventListenerAdapter(annotatedHandler);
        return annotationEventListenerAdapter;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        
    }

    private boolean isPostProcessingCandidate(Class<?> targetClass) {

        return hasEventHandlerMethod(targetClass);
    }

    private boolean isNotEventHandlerSubclass(Class<?> targetClass) {
        return !EventListener.class.isAssignableFrom(targetClass);
    }

    private boolean hasEventHandlerMethod(Class<?> beanClass) {
        final AtomicBoolean result = new AtomicBoolean(false);
        ReflectionUtils.doWithMethods(beanClass, new HasEventHandlerAnnotationMethodCallback(result));
        return result.get();
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private class HasEventHandlerAnnotationMethodCallback implements ReflectionUtils.MethodCallback {

        private final AtomicBoolean result;

        public HasEventHandlerAnnotationMethodCallback(AtomicBoolean result) {
            this.result = result;
        }

        @Override
        public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
            if (method.isAnnotationPresent(EventHandler.class)) {
                result.set(true);
            }
        }
    }

    private class AdapterIntroductionInterceptor implements IntroductionInfo, IntroductionInterceptor {

        private AnnotationEventListenerAdapter annotationEventListenerAdapter;

        public AdapterIntroductionInterceptor(AnnotationEventListenerAdapter annotationEventListenerAdapter) {

            this.annotationEventListenerAdapter = annotationEventListenerAdapter;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return invocation.getMethod().invoke(annotationEventListenerAdapter, invocation.getArguments());
        }

        @Override
        public boolean implementsInterface(Class aClass) {
            return true;
        }

        @Override
        public Class[] getInterfaces() {
            return new Class[]{EventListener.class};
        }
    }
}
