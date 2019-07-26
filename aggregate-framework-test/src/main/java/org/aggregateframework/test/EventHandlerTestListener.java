package org.aggregateframework.test;

import com.google.common.collect.Maps;
import org.aggregateframework.eventhandling.EventHandlerListener;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;

class EventHandlerTestListener implements EventHandlerListener {

    static final Logger logger = Logger.getLogger(EventHandlerTestListener.class.getSimpleName());

    private EventHandlerRetryTestHandler methodHook;
    private Map<String, EventHandlerRetryConfig> methodConfig;

    public EventHandlerTestListener() {
        methodHook = new EventHandlerRetryTestHandler();
        methodConfig = Maps.newHashMap();
    }

    public void addWatchedMethodName(String className, String methodName, EventHandlerRetryConfig config) {
        methodConfig.put(className+"." + methodName, config);
    }

    @Override
    public boolean isActive() {
        return true;
    }



    @Override
    public void before(Object target, Method method, Object[] params) throws Exception {

        EventHandlerRetryConfig config = methodConfig.get(AOPClassNameUtils.getClassMethodName(target, method));
        if (config != null) {
            methodHook.interceptEventHandlerMethod(config, target, method, params);
        }
    }

    @Override
    public void after(Object target, Method method, Object[] params, Exception e) throws Exception {
        EventHandlerRetryConfig config = methodConfig.get(AOPClassNameUtils.getClassMethodName(target, method));
        if (config != null) {
            methodHook.onAfterHandler(config, target, method, params, e);
        }
    }
}
