package org.aggregateframework.sample.inmemory.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;

/**
 * Created by changmingxie on 11/8/15.
 */
@Aspect
public class AdapterAspect implements Ordered {

    private int order = Ordered.HIGHEST_PRECEDENCE + 1;


    @Pointcut("execution(public * void write(Cache.Entry<? extends Long, ? extends Payment> entry) throws CacheWriterException)")
    public void writeCall() {

    }

    @Around("writeCall()")
    public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("ok");
        return null;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
