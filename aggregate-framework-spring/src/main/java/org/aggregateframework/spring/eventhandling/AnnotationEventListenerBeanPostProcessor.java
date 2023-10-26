package org.aggregateframework.spring.eventhandling;

import org.aggregateframework.eventbus.EventBus;
import org.aggregateframework.eventbus.SimpleEventBus;
import org.aggregateframework.eventhandling.AnnotationEventListenerAdapter;
import org.aggregateframework.eventhandling.EventListener;
import org.aggregateframework.eventhandling.annotation.EventHandler;
import org.aggregateframework.eventhandling.processor.AsyncMethodInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: changming.xie
 * Date: 14-6-13
 * Time: 上午10:46
 */
public class AnnotationEventListenerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationEventListenerBeanPostProcessor.class);

    private EventBus eventBus = SimpleEventBus.INSTANCE;
    ;
    private ApplicationContext applicationContext;

    public AnnotationEventListenerBeanPostProcessor() {
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
        eventBus.subscribe(proxy);
    }

    private EventListener createEventListenerProxy(Object annotatedHandler, boolean proxyTargetClass, ClassLoader classLoader) {
        return new AnnotationEventListenerAdapter(annotatedHandler);
    }

    private boolean isPostProcessingCandidate(Class<?> targetClass) {

        return hasEventHandlerMethod(targetClass);
    }

    private boolean hasEventHandlerMethod(Class<?> beanClass) {
        final AtomicBoolean result = new AtomicBoolean(false);
        ReflectionUtils.doWithMethods(beanClass, new HasEventHandlerAnnotationMethodCallback(result));
        return result.get();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        AsyncMethodInvoker asyncMethodInvoker = AsyncMethodInvoker.getInstance();

        logger.info("aggeraget-framework disruptor and retry-disruptor shutdown...");

        asyncMethodInvoker.shutdown();

        logger.info("aggeraget-framework disruptor and retry-disruptor shutdowned");

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
}
