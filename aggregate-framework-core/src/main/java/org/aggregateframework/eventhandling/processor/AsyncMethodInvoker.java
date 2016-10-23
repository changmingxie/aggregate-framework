package org.aggregateframework.eventhandling.processor;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.dsl.Disruptor;
import org.aggregateframework.SystemException;
import org.aggregateframework.context.AsyncParameterConfig;
import org.aggregateframework.context.PayloadDisruptorConfig;
import org.aggregateframework.eventhandling.EventInvokerEntry;
import org.aggregateframework.eventhandling.annotation.Retryable;
import org.aggregateframework.eventhandling.processor.async.AsyncEvent;
import org.aggregateframework.eventhandling.processor.async.AsyncEventFactory;
import org.aggregateframework.eventhandling.processor.async.RetryEvent;
import org.aggregateframework.eventhandling.processor.async.RetryEventFactory;
import org.aggregateframework.eventhandling.processor.retry.*;
import org.aggregateframework.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by changmingxie on 12/2/15.
 */
public class AsyncMethodInvoker {

    static final Logger logger = LoggerFactory.getLogger(AsyncMethodInvoker.class);

    private static Map<Class, Disruptor<AsyncEvent>> payloadTypeDisruptorMap = new ConcurrentHashMap<Class, Disruptor<AsyncEvent>>();

    private static Map<Class, Disruptor<RetryEvent>> payloadTypeRetryDisruptorMap = new ConcurrentHashMap<Class, Disruptor<RetryEvent>>();

    private static Map<Disruptor<AsyncEvent>, NotFullCondition> disruptorNotFullConditionMap = new ConcurrentHashMap<Disruptor<AsyncEvent>, NotFullCondition>();

    private static Map<Disruptor<RetryEvent>, NotFullCondition> retryDisruptorNotFullConditionMap = new ConcurrentHashMap<Disruptor<RetryEvent>, NotFullCondition>();

    private static volatile AsyncMethodInvoker INSTANCE = null;


    public static AsyncMethodInvoker getInstance() {

        if (INSTANCE == null) {

            synchronized (AsyncMethodInvoker.class) {

                if (INSTANCE == null) {
                    INSTANCE = new AsyncMethodInvoker();
                }
            }
        }

        return INSTANCE;
    }

    public void invoke(EventInvokerEntry eventInvokerEntry) {
        Method method = eventInvokerEntry.getMethod();
        Object target = eventInvokerEntry.getTarget();
        Object[] params = eventInvokerEntry.getParams();

        Disruptor<AsyncEvent> disruptor = getDisruptor(eventInvokerEntry.getPayloadType());

        if (disruptor.getRingBuffer().remainingCapacity() <= 0) {
            logger.warn(String.format("aggregate framework ring buffer size fulled. remaining capacity:%d, method:%s", disruptor.getRingBuffer().remainingCapacity(), method.getName()));
        }

        long sequence;

        if ((sequence = tryNext(disruptor)) == -1) {

            Method recoverMethod = tryGetRecoverMethod(target, method);
            if (recoverMethod != null) {
                try {
                    recoverMethod.invoke(target, params);
                } catch (Exception ex) {
                    throw new SystemException(ex);
                }
                return;

            } else {

                disruptorNotFullConditionMap.get(disruptor).lock.lock();

                try {
                    while ((sequence = tryNext(disruptor)) == -1) {
                        try {

                            //add timeout to ensure the thread can run finally
                            disruptorNotFullConditionMap.get(disruptor).notFull.await(2, TimeUnit.SECONDS);
                            //as notfull.signial call at onEvent method,
                            // which cannot ensure the sequence is release immediately, so wait a moment
//                            LockSupport.parkNanos(1);
                        } catch (InterruptedException e) {

                        }
                    }
                } finally {
                    disruptorNotFullConditionMap.get(disruptor).lock.unlock();
                }
            }
        }

        try {
            AsyncEvent event = disruptor.getRingBuffer().get(sequence);
            event.reset(eventInvokerEntry.getPayloadType(), method, target, params);
        } finally {
            disruptor.getRingBuffer().publish(sequence);
        }

        return;
    }


    public void shutdown() {

        for (Map.Entry<Class, Disruptor<AsyncEvent>> entry : payloadTypeDisruptorMap.entrySet()) {
            entry.getValue().shutdown();
        }

        for (Map.Entry<Class, Disruptor<RetryEvent>> entry : payloadTypeRetryDisruptorMap.entrySet()) {
            entry.getValue().shutdown();
        }
    }

    private AsyncMethodInvoker() {

    }

    private Method tryGetRecoverMethod(Object target, Method method) {

        Retryable retryable = ReflectionUtils.getAnnotation(method, Retryable.class);

        if (retryable != null && StringUtils.isNotEmpty(retryable.recoverMethod())) {
            try {
                return target.getClass().getMethod(retryable.recoverMethod(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
        return null;
    }

    private long tryNext(Disruptor disruptor) {
        try {
            long sequence = disruptor.getRingBuffer().tryNext();
            return sequence;
        } catch (InsufficientCapacityException e) {
            return -1;
        }
    }

    private Disruptor<AsyncEvent> initializeAsyncRingBuffer(Class payloadType) {

        PayloadDisruptorConfig payloadDisruptorConfig = getPayloadDisruptorConfig(payloadType);

        Executor executor = payloadDisruptorConfig.getExecutor();

        AsyncEventFactory factory = new AsyncEventFactory();

        Disruptor<AsyncEvent> disruptor = new Disruptor<AsyncEvent>(factory, payloadDisruptorConfig.getRingBufferSize(), executor);

        disruptor.handleExceptionsWith(new AsyncExceptionEventHandler());

        AsyncEventHandler asyncEventHandler = new AsyncEventHandler();

        AsyncEventHandler[] asyncEventHandlers = new AsyncEventHandler[payloadDisruptorConfig.getWorkPoolSize()];

        for (int i = 0; i < asyncEventHandlers.length; i++) {
            asyncEventHandlers[i] = asyncEventHandler;
        }

        disruptor.handleEventsWithWorkerPool(asyncEventHandlers);

        disruptor.start();
        return disruptor;
    }

    private PayloadDisruptorConfig getPayloadDisruptorConfig(Class payloadType) {

        PayloadDisruptorConfig payloadDisruptorConfig = null;

        for (PayloadDisruptorConfig config : AsyncParameterConfig.PAYLOAD_TYPE_DISRUPTOR_CONFIGS) {
            if (config.getPayloadType().equals(payloadType)) {
                payloadDisruptorConfig = config;
            }
        }

        if (payloadDisruptorConfig == null) {
            payloadDisruptorConfig = new PayloadDisruptorConfig(payloadType);
        }
        return payloadDisruptorConfig;
    }

    private Disruptor<RetryEvent> initializeRetryRingBuffer(Class payloadType) {

        PayloadDisruptorConfig payloadDisruptorConfig = getPayloadDisruptorConfig(payloadType);

        RetryEventFactory factory = new RetryEventFactory();

        Disruptor<RetryEvent> retryDisruptor = new Disruptor<RetryEvent>(factory, payloadDisruptorConfig.getRingBufferSize(), payloadDisruptorConfig.getExecutor());

        retryDisruptor.handleExceptionsWith(new RetryExceptionEventHandler());

        RetryEventHandler retryEventHandler = new RetryEventHandler();

        RetryEventHandler[] retryEventHandlers = new RetryEventHandler[payloadDisruptorConfig.getWorkPoolSize()];

        for (int i = 0; i < retryEventHandlers.length; i++) {
            retryEventHandlers[i] = retryEventHandler;
        }

        retryDisruptor.handleEventsWithWorkerPool(retryEventHandlers);

        retryDisruptor.start();

        return retryDisruptor;
    }


    private Disruptor<AsyncEvent> getDisruptor(Class payloadType) {

        if (!payloadTypeDisruptorMap.containsKey(payloadType)) {

            synchronized (payloadType) {

                if (!payloadTypeDisruptorMap.containsKey(payloadType)) {

                    Disruptor<AsyncEvent> disruptor = initializeAsyncRingBuffer(payloadType);

                    payloadTypeDisruptorMap.put(payloadType, disruptor);

                    disruptorNotFullConditionMap.put(disruptor, new NotFullCondition());

                }
            }
        }

        return payloadTypeDisruptorMap.get(payloadType);

    }

    private Disruptor<RetryEvent> getRetryDisruptor(Class payloadType) {

        if (!payloadTypeRetryDisruptorMap.containsKey(payloadType)) {

            synchronized (payloadType) {

                if (!payloadTypeRetryDisruptorMap.containsKey(payloadType)) {

                    Disruptor<RetryEvent> retryDisruptor = initializeRetryRingBuffer(payloadType);

                    payloadTypeRetryDisruptorMap.put(payloadType, retryDisruptor);

                    retryDisruptorNotFullConditionMap.put(retryDisruptor, new NotFullCondition());
                }
            }
        }

        return payloadTypeRetryDisruptorMap.get(payloadType);

    }

    private void retryableInvoke(Throwable throwable, AsyncEvent asyncEvent) {

        Disruptor<RetryEvent> retryDisruptor = getRetryDisruptor(asyncEvent.getPayloadType());

        long sequence = -1;

        if ((sequence = tryNext(retryDisruptor)) == -1) {

            retryDisruptorNotFullConditionMap.get(retryDisruptor).lock.lock();
            try {
                while ((sequence = tryNext(retryDisruptor)) == -1) {
                    try {

                        //add timeout to ensure the thread can run finally
                        retryDisruptorNotFullConditionMap.get(retryDisruptor).notFull.await(2, TimeUnit.SECONDS);

                        //as notfull.signial call at onEvent method,
                        // which cannot ensure the sequence is release immediately, so wait a moment
//                        LockSupport.parkNanos(1000 * 1000 * 100);
                    } catch (InterruptedException e) {

                    }
                }
            } finally {
                retryDisruptorNotFullConditionMap.get(retryDisruptor).lock.unlock();
            }
        }

        try {
            RetryEvent event = retryDisruptor.getRingBuffer().get(sequence);
            event.reset(asyncEvent.getPayloadType(), throwable, asyncEvent.getMethod(), asyncEvent.getTarget(), asyncEvent.getParams());
        } finally {
            retryDisruptor.getRingBuffer().publish(sequence);
        }
    }

    private void conditionSignalAll(NotFullCondition notFullCondition) {
        notFullCondition.lock.lock();
        try {
            notFullCondition.notFull.signalAll();
        } finally {
            notFullCondition.lock.unlock();
        }
    }

    class AsyncEventHandler implements com.lmax.disruptor.WorkHandler<AsyncEvent> {

        @Override
        public void onEvent(AsyncEvent asyncEvent) throws Exception {
            asyncEvent.getMethod().invoke(asyncEvent.getTarget(), asyncEvent.getParams());

            Disruptor<AsyncEvent> disruptor = payloadTypeDisruptorMap.get(asyncEvent.getPayloadType());
            conditionSignalAll(disruptorNotFullConditionMap.get(disruptor));
        }
    }

    class RetryEventHandler implements com.lmax.disruptor.WorkHandler<RetryEvent> {

        @Override
        public void onEvent(final RetryEvent retryEvent) throws Exception {

            final Retryable retryable = ReflectionUtils.getAnnotation(retryEvent.getMethod(), Retryable.class);

            final PolicyBuilder policyBuilder = new PolicyBuilder();
            RetryTemplate retryTemplate = new RetryTemplate();

            RetryPolicy retryPolicy = policyBuilder.getRetryPolicy(retryable);
            retryTemplate.setRetryPolicy(retryPolicy);
            retryTemplate.setBackOffPolicy(policyBuilder.getBackoffPolicy(retryable.backoff()));

            RetryContext retryContext = retryPolicy.requireRetryContext();

            retryContext.registerThrowable(retryEvent.getThrowable());

            final Method method = retryEvent.getMethod();
            final Object target = retryEvent.getTarget();
            final Object[] params = retryEvent.getParams();

            RetryCallback<Object> retryCallback = new RetryCallback<Object>() {
                @Override
                public Object doWithRetry(RetryContext context) {
                    try {
                        return method.invoke(target, params);
                    } catch (IllegalAccessException e) {
                        throw new SystemException(e);
                    } catch (InvocationTargetException e) {
                        throw new SystemException(e);
                    }
                }
            };

            RecoveryCallback<Object> recoveryCallback = null;

            if (StringUtils.isNotEmpty(retryable.recoverMethod())) {
                recoveryCallback = new SimpleRecoveryCallback(target, retryable.recoverMethod(), method.getParameterTypes(), params);
            }

            retryTemplate.execute(retryContext, retryCallback, recoveryCallback);

            Disruptor<RetryEvent> disruptor = payloadTypeRetryDisruptorMap.get(retryEvent.getPayloadType());
            conditionSignalAll(retryDisruptorNotFullConditionMap.get(disruptor));
        }

    }


    class AsyncExceptionEventHandler implements ExceptionHandler<AsyncEvent> {

        @Override
        public void handleEventException(Throwable throwable, long l, AsyncEvent asyncEvent) {

            try {
                Retryable retryable = ReflectionUtils.getAnnotation(asyncEvent.getMethod(), Retryable.class);

                if (retryable == null) {
                    logger.error(String.format("method call failed. method:%s,target:%s", asyncEvent.getMethod().getName(), asyncEvent.getTarget().toString()), throwable);
                } else {
                    retryableInvoke(throwable, asyncEvent);
                }
            } finally {
                Disruptor<AsyncEvent> disruptor = payloadTypeDisruptorMap.get(asyncEvent.getPayloadType());
                conditionSignalAll(disruptorNotFullConditionMap.get(disruptor));
            }
        }

        @Override
        public void handleOnStartException(Throwable throwable) {
            throw new Error(throwable);
        }

        @Override
        public void handleOnShutdownException(Throwable throwable) {
            throw new Error(throwable);
        }
    }

    class RetryExceptionEventHandler implements ExceptionHandler<RetryEvent> {

        @Override
        public void handleEventException(Throwable throwable, long l, RetryEvent retryEvent) {

            try {
                logger.error(String.format("method call failed. method:%s,target:%s", retryEvent.getMethod().getName(), retryEvent.getTarget().toString()), throwable);
            } finally {
                Disruptor<RetryEvent> disruptor = payloadTypeRetryDisruptorMap.get(retryEvent.getPayloadType());
                conditionSignalAll(retryDisruptorNotFullConditionMap.get(disruptor));
            }
        }

        @Override
        public void handleOnStartException(Throwable throwable) {
            throw new Error(throwable);
        }

        @Override
        public void handleOnShutdownException(Throwable throwable) {
            throw new Error(throwable);
        }
    }

    class NotFullCondition {

        final ReentrantLock lock = new ReentrantLock();

        final Condition notFull = lock.newCondition();
    }

}
