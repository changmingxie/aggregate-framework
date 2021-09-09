package org.aggregateframework.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by changming.xie on 2/23/17.
 */
public final class FactoryBuilder {


    private FactoryBuilder() {

    }

    private static final List<BeanFactory> beanFactories = new ArrayList<BeanFactory>();

    private static final ConcurrentHashMap<Class, SingeltonFactory> classFactoryMap = new ConcurrentHashMap<>();

    public static <T> SingeltonFactory<T> factoryOf(Class<T> clazz) {

        if (!classFactoryMap.containsKey(clazz)) {

            for (BeanFactory beanFactory : beanFactories) {
                if (beanFactory.isFactoryOf(clazz)) {
                    classFactoryMap.putIfAbsent(clazz, new SingeltonFactory<T>(clazz, beanFactory));
                }
            }
        }

        return classFactoryMap.get(clazz);
    }

    public static BeanFactory getFactory(Class type) {
        for (BeanFactory beanFactory : beanFactories) {
            if (type.isAssignableFrom(beanFactory.getClass())) {
                return beanFactory;
            }
        }
        return null;
    }

    public static void registerBeanFactory(BeanFactory beanFactory) {
        beanFactories.add(beanFactory);
    }

    public static class SingeltonFactory<T> {

        private final BeanFactory beanFactory;

        private final Class<T> clazz;

        public SingeltonFactory(Class<T> clazz, BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
            this.clazz = clazz;
        }

        public T getInstance() {

//            if (instance == null) {
//                synchronized (SingeltonFactory.class) {
//                    if (instance == null) {
//                        try {
//                            ClassLoader loader = Thread.currentThread().getContextClassLoader();
//
//                            Class<?> clazz = loader.loadClass(className);
//
//                            instance = (T) clazz.newInstance();
//                        } catch (Exception e) {
//                            throw new RuntimeException("Failed to create an instance of " + className, e);
//                        }
//                    }
//                }
//            }

            return beanFactory.getBean(clazz);
        }

        public T getInstance(String name) {
            return beanFactory.getBean(name, clazz);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;

            SingeltonFactory that = (SingeltonFactory) other;
    
            return clazz.equals(that.clazz);
        }

        @Override
        public int hashCode() {
            return clazz.hashCode();
        }


    }
}