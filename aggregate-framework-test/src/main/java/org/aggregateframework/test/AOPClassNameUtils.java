package org.aggregateframework.test;

import java.lang.reflect.Method;

public class AOPClassNameUtils {
    // AOP切面会修改类名,比如SampleClass会被改成SampleClass$$EnhancerBySpringCGLIB$$0xaabb3344
    // 这个函数可以把 SampleClass$$EnhancerBySpringCGLIB$$0xaabb3344 ===还原成===> SampleClass
    public static String recoverAOPClassName(Object target) {
        String klassName = target.getClass().getSimpleName();
        int index = klassName.indexOf("$$EnhancerBySpringCGLIB");
        if (index >= 0) {
            klassName = klassName.substring(0, index);
        }
        return klassName;
    }

    // SampleClass$$EnhancerBySpringCGLIB$$0xaabb3344.fun => SampleClass.fun
    public static String getClassMethodName(Object target, Method method) {
        return recoverAOPClassName(target) + "." + method.getName();
    }
}
