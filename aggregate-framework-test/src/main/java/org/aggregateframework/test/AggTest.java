package org.aggregateframework.test;

import org.aggregateframework.eventhandling.EventHandlerHook;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;

public class AggTest {

    private static EventHandlerTestListener listener;

    protected static Exception lastException = null;

    static void installListener() {
        if (listener == null) {
            listener = new EventHandlerTestListener();
            EventHandlerHook.INSTANCE.addListener(listener);
        }
    }

    public static void Wait() throws Exception {
        synchronized(AggTest.class) {
            AggTest.class.wait();
        }

        Thread.sleep(3 * 1000L);

        Log logger = LogFactory.getLog(AggTest.class);
        if (lastException != null) {
            logger.info("AGG重试测试结束，最后发生异常 "+lastException.getClass().getName()+" 导致测试失败");
            throw lastException;
        } else {
            logger.info("AGG重试测试结束,测试通过！！！");
        }
    }

    protected static boolean hasMethod(Class<?> klass, String methodName) {
        for(Method method : klass.getMethods()) {
            if (method.getName().equals(methodName)) {
                // check method exists
                return true;
            }
        }
        return false;
    }

    public static Register getRegister() {
        return new Register();
    }

    public static class Register {
        Class<?> klass;
        String methodName;
        EventHandlerRetryConfig config = new EventHandlerRetryConfig();

        public void regist() {
            installListener();
            listener.addWatchedMethodName(klass.getSimpleName(), methodName, config);
        }

        public Register() {
            config.setEventUniqueKey("");
            config.setFailCount(1);
            config.setShowDebugMessage(true);
            config.setThrowException(EventHandlerTestException.class);
        }

        public Register withClassMethod(Class<?> klass, String methodName) throws NoSuchMethodException {
            if (!hasMethod(klass, methodName)) {
                throw new NoSuchMethodException(methodName);
            }
            this.klass = klass;
            this.methodName = methodName;
            return this;
        }

        public Register withFailCount(int count) {
            config.setFailCount(count);
            return this;
        }

        public Register withEventUniqueKey(String eventKey) {
            config.setEventUniqueKey(eventKey);
            return this;
        }

        public Register withThrowException(Class<? extends Exception> throwException) {
            config.setThrowException(throwException);
            return this;
        }

        public Register withShowDebugMessage(boolean show) {
            config.setShowDebugMessage(show);
            return this;
        }
    }

    public static void RegistEventHandler(Class<?> klass, String methodName) throws NoSuchMethodException {
        RegistEventHandler(klass, methodName, 1, "", EventHandlerTestException.class);
    }

    public static void RegistEventHandler(Class<?> klass, String methodName, int failedCount, String eventKey, Class<? extends Exception> throwException) throws NoSuchMethodException {
        installListener();

        if (!hasMethod(klass, methodName)) {
            throw new NoSuchMethodException(methodName);
        }

        EventHandlerRetryConfig config = new EventHandlerRetryConfig();
        config.setEventUniqueKey(eventKey);
        config.setFailCount(failedCount);
        config.setShowDebugMessage(true);
        config.setThrowException(throwException);
        listener.addWatchedMethodName(klass.getSimpleName(), methodName, config);
    }
}
