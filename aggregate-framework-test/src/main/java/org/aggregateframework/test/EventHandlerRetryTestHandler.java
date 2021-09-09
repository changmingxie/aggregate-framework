package org.aggregateframework.test;

import org.aggregateframework.entity.DomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class EventHandlerRetryTestHandler {
    
    static final Logger logger = LoggerFactory.getLogger(EventHandlerRetryTestHandler.class.getSimpleName());
    
    private final Map<String, Map<Object, MethodEventCallCounter>> tryCount = new LinkedHashMap<String, Map<Object, MethodEventCallCounter>>();
    
    /*
     * 安全性保护，如果不小心把EventHandlerTest发布到生产环境，模拟失败最多产生20次，避免造成大量损失
     */
    private final int totalTryLimits = 20;
    private       int totalTry       = 0;
    
    private Exception lastException = null;
    
    private final String bannerStart = "\n------------------------ @EventHandlerRetryTest start ------------------------\n";
    private final String bannerEnd   = "\n------------------------ @EventHandlerRetryTest end   ------------------------\n";
    
    private Object getKeyFromEvent(boolean verbose, String keyString, Object[] args) throws IllegalAccessException {
        if (args.length == 0) {
            return keyString;
        }
        
        if (keyString.isEmpty()) {
            
            for (Field field : args[0].getClass().getDeclaredFields()) {
                if (DomainObject.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    DomainObject v = (DomainObject) field.get(args[0]);
                    return v.getId();
                }
            }
            
            return keyString;
            
        } else {
            
            EvaluationContext context = new StandardEvaluationContext();  // 表达式的上下文,
            context.setVariable("event", args[0]);                     // 为了让表达式可以访问该对象, 先把对象放到上下文中
            context.setVariable("args", args);
            ExpressionParser parser = new SpelExpressionParser();
            
            Object value = parser.parseExpression(keyString).getValue(context);
            
            if (DomainObject.class.isAssignableFrom(value.getClass())) {
                value = ((DomainObject) value).getId();
            }
            
            if (verbose) {
                StringBuffer sb = new StringBuffer();
                sb.append(bannerStart)
                  .append("| el表达式：" + keyString + "\n")
                  .append("| 事件：" + args[0].getClass().getSimpleName())
                  .append(" = ");
                if (value == null) {
                    sb.append("null\n")
                      .append("| 提示：el表达式配置可能有问题，事例如：")
                      .append("|     #event.propertyA.propertyB")
                      .append("|     #args[0].propertyA.propertyB");
                } else {
                    sb.append(value);
                }
                sb.append(bannerEnd);
                logger.info(sb.toString());
            }
            
            return value;
        }
    }
    
    private boolean prodProtect() {
        if (totalTry >= totalTryLimits) {
            StringBuffer sb = new StringBuffer();
            sb.append(bannerStart).append("已达到最大模拟失败次数20次，该进程以后不会再模拟失败，如需测试请重启进程").append(bannerEnd);
            logger.info(sb.toString());
            
            return true;
        }
        
        return false;
    }
    
    private boolean neverFail(EventHandlerRetryConfig config, Object target, Method method) {
        if (config.getFailCount() <= 0) {
            
            if (config.isShowDebugMessage()) {
                
                StringBuffer sb = new StringBuffer();
                sb.append(bannerStart)
                  .append("{")
                  .append(target.getClass().getSimpleName())
                  .append(".")
                  .append(method.getName())
                  .append("} 模拟失败次数配置<=0，直接返回成功")
                  .append(bannerEnd);
                logger.info(sb.toString());
            }
            
            return true;
        }
        
        return false;
    }
    
    private MethodEventCallCounter getCallCount(Object target, Method method, Object eventKey, Integer failCount) {
        MethodEventCallCounter counter;
        Map<Object, MethodEventCallCounter> methodCount = tryCount.get(AOPClassNameUtils.getClassMethodName(target, method));
        if (methodCount == null) {
            methodCount = new LinkedHashMap<Object, MethodEventCallCounter>();
            tryCount.put(AOPClassNameUtils.getClassMethodName(target, method), methodCount);
        }
        
        counter = methodCount.get(eventKey);
        if (counter == null) {
            counter                 = new MethodEventCallCounter();
            counter.callCount       = new AtomicInteger(0);
            counter.expectFailCount = failCount;
            methodCount.put(eventKey, counter);
        }
        return counter;
    }
    
    public void interceptEventHandlerMethod(EventHandlerRetryConfig config, Object target, Method method, Object[] args) throws Exception {
        synchronized (this) {
            Object eventKey = getKeyFromEvent(config.isShowDebugMessage(), config.getEventUniqueKey(), args);
            
            if (prodProtect()) {
                return;
            }
            
            if (neverFail(config, target, method)) {
                return;
            }
            
            totalTry += 1;
            MethodEventCallCounter counter = getCallCount(target, method, eventKey, config.failCount);
            counter.callCount.incrementAndGet();
            
            if (counter.callCount.get() < config.getFailCount()) {
                throw config.getThrowException().getConstructor().newInstance();
            }
        }
    }
    
    public void onAfterHandler(EventHandlerRetryConfig config,
                               Object target,
                               Method method,
                               Object[] args,
                               Exception e) throws IllegalAccessException {
        synchronized (this) {
            Object eventKey = getKeyFromEvent(config.isShowDebugMessage(), config.getEventUniqueKey(), args);
            MethodEventCallCounter counter = getCallCount(target, method, eventKey, config.failCount);
            
            int totalExceptCallCount = 0;
            int happenCallCount = 0;
            int MethodEventCalls = 0;
            for (Map.Entry<String, Map<Object, MethodEventCallCounter>> methodKV : tryCount.entrySet()) {
                for (Map.Entry<Object, MethodEventCallCounter> eventKV : methodKV.getValue().entrySet()) {
                    MethodEventCallCounter tmp = eventKV.getValue();
                    totalExceptCallCount += tmp.expectFailCount;
                    happenCallCount += tmp.callCount.get();
                    ++MethodEventCalls;
                }
            }
            
            lastException = e;
            
            if (config.isShowDebugMessage()) {
                String classMethodName = target.getClass().getSimpleName() + "." + method.getName();
                StringBuffer sb = buildCallMethodInfo(config, classMethodName, args[0], eventKey, counter, MethodEventCalls);
                logger.info(sb.toString());
                
                try {
                    this.wait(5); // 等待，防止上面打印log信息的时候被抛异常的信息打乱
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            
            if (happenCallCount >= totalExceptCallCount) {
                AggTest.lastException = lastException;
                synchronized (AggTest.class) {
                    AggTest.class.notify();
                }
            }
        }
    }
    
    private StringBuffer buildCallMethodInfo(EventHandlerRetryConfig config,
                                             String str,
                                             Object arg,
                                             Object eventKey,
                                             MethodEventCallCounter counter,
                                             int registCalls) {
        StringBuffer sb = new StringBuffer();
        sb.append(bannerStart)
          .append("------------------------------ 本次调用情况{" + totalTry + "} ------------------------------\n")
          .append("| 处理函数：")
          .append(str)
          .append("\n| 事件信息：")
          .append(arg.getClass().getSimpleName())
          .append("<" + eventKey + "> ")
          .append("\n| 重试次数： [" + counter.callCount + "/" + config.getFailCount() + "]\n");
        
        boolean shouldSuccess = false;
        if (counter.callCount.intValue() < config.getFailCount()) {
            sb.append("| 模拟行为：本次调用将模拟抛出异常 " + config.getThrowException().getSimpleName());
        } else if (counter.callCount.intValue() == config.getFailCount()) {
            sb.append("| 模拟行为：本次调用应该成功");
            shouldSuccess = true;
        } else {
            sb.append("| 模拟行为：EventHandler实际重试次数已经大于模拟设置次数，请检查EventHandler执行是否抛出异常");
            shouldSuccess = true;
        }
        
        if (lastException == null) {
            sb.append("\n| 实际情况：执行成功，未发生异常");
        } else {
            sb.append("\n| 实际情况：执行失败,发生异常：" + lastException.getClass().getSimpleName());
            if (shouldSuccess || lastException.getClass() != config.throwException) {
                sb.append(",这肯定是出问题啦，赶快查一查！！!");
            } else {
                sb.append(",这个异常是期望的结果,没有问题");
            }
        }
        
        if (registCalls > 1) {
            sb.append("\n--------------------------- 所有监测的调用情况如下 ------------------------------\n");
            for (Map.Entry<String, Map<Object, MethodEventCallCounter>> methodKV : tryCount.entrySet()) {
                for (Map.Entry<Object, MethodEventCallCounter> eventKV : methodKV.getValue().entrySet()) {
                    MethodEventCallCounter tmp = eventKV.getValue();
                    sb.append("| 处理函数：").append(methodKV.getKey());
                    if (tmp == counter) {
                        sb.append("   <====== 这就是本次调用的函数");
                    }
                    
                    sb.append("\n| 事件信息：<").append(eventKV.getKey() + ">")
                      .append("\n| 重试次数： [" + tmp.callCount + "/" + tmp.expectFailCount + "]\n");
                }
            }
        }
        
        sb.append(bannerEnd);
        return sb;
    }
    
    static class MethodEventCallCounter {
        public AtomicInteger callCount;
        public Integer       expectFailCount;
    }
}
