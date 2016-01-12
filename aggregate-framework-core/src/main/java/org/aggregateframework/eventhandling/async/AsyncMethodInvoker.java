package org.aggregateframework.eventhandling.async;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.aggregateframework.SystemException;
import org.aggregateframework.context.AsyncParameterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Created by changmingxie on 12/2/15.
 */
public class AsyncMethodInvoker {

    private static final Logger logger = LoggerFactory.getLogger(AsyncMethodInvoker.class);

    private final RingBuffer<AsyncEvent> ringBuffer;

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


    private AsyncMethodInvoker() {

        Executor executor = AsyncParameterConfig.EXECUTOR;

        AsyncEventFactory factory = new AsyncEventFactory();

        Disruptor<AsyncEvent> disruptor = new Disruptor<AsyncEvent>(factory, AsyncParameterConfig.DISRUPTOR_RING_BUFFER_SIZE, executor);

        disruptor.handleExceptionsWith(new AsyncExceptionEventHandler());

        disruptor.handleEventsWith(new AsyncEventHandler());

        disruptor.start();

        ringBuffer = disruptor.getRingBuffer();
    }

    public void invoke(Method method, Object target, Object... params) {
        long sequence = ringBuffer.next();

        try {
            AsyncEvent event = ringBuffer.get(sequence);
            event.reset(method, target, params);
        } finally {
            ringBuffer.publish(sequence);
        }
    }


    class AsyncEventHandler implements com.lmax.disruptor.EventHandler<AsyncEvent> {

        @Override
        public void onEvent(AsyncEvent asyncEvent, long l, boolean b) throws Exception {
            try {
                asyncEvent.getMethod().invoke(asyncEvent.getTarget(), asyncEvent.getParams());
            } catch (IllegalAccessException e) {
                throw new SystemException(e);
            } catch (InvocationTargetException e) {
                throw new SystemException(e);
            }
        }
    }

    class AsyncExceptionEventHandler implements ExceptionHandler<AsyncEvent> {

        @Override
        public void handleEventException(Throwable throwable, long l, AsyncEvent asyncEvent) {
            logger.error(String.format("method call failed. method:%s,target:%s", asyncEvent.getMethod().getName(), asyncEvent.getTarget().toString()), throwable);
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

}
