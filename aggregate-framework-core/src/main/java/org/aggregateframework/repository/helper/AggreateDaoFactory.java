package org.aggregateframework.repository.helper;

import org.aggregateframework.NoDaoDefinitionException;
import org.aggregateframework.SystemException;
import org.aggregateframework.dao.AggregateDao;
import org.aggregateframework.dao.DomainObjectDao;
import org.aggregateframework.entity.AbstractDomainObject;
import org.aggregateframework.entity.DomainObject;
import org.aggregateframework.factory.BeanFactory;
import org.aggregateframework.factory.DaoFactory;
import org.aggregateframework.factory.FactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author changming.xie
 */
public class AggreateDaoFactory {

    private static final Logger logger = LoggerFactory.getLogger(AggreateDaoFactory.class);

    private static final Map<Class<? extends DomainObject>, DomainObjectDao> daoMap = new ConcurrentHashMap<Class<? extends DomainObject>, DomainObjectDao>();

    public synchronized static <E extends DomainObject<ID>, ID extends Serializable> DomainObjectDao<E, ID> getDao(Class<E> entityClass) {

        DomainObjectDao<E, ID> foundDao = null;

        foundDao = daoMap.get(entityClass);

        if (foundDao == null) {

            BeanFactory beanFactory = FactoryBuilder.getFactory(DaoFactory.class);

            if (beanFactory == null) {
                throw new SystemException("Couldn't get DaoFactory, maybe integration with web application container is not enabled, check whether @EnableSpringIntegration、<agg:integeration /> or SpringIntegrationConfiguration is configured or not.");
            }

            Map<String, DomainObjectDao> daos = FactoryBuilder.getFactory(DaoFactory.class).getBeansOfType(DomainObjectDao.class);

            for (DomainObjectDao<E, ID> dao : daos.values()) {

                AggregateDao aggregateDao = AnnotationUtils.findAnnotation(dao.getClass(), AggregateDao.class);

                if (aggregateDao != null && isEntityClassMatch(aggregateDao.value(), entityClass)) {
                    foundDao = dao;
                    daoMap.put(entityClass, foundDao);
                    return foundDao;
                }

                Class[] classes = dao.getClass().getInterfaces();

                for (Class clazz : classes) {

                    Type[] types = clazz.getGenericInterfaces();

                    for (Type type : types) {

                        if (type instanceof ParameterizedType) {

                            ParameterizedType parameterizedType = (ParameterizedType) type;

                            Type[] typeArguments = parameterizedType.getActualTypeArguments();

                            for (Type typeArgument : typeArguments) {
                                if (isEntityClassMatch(typeArgument, entityClass)) {
                                    foundDao = dao;
                                    daoMap.put(entityClass, foundDao);
                                    return foundDao;
                                }
                            }
                        }
                    }

                }

            }
        }

        if (foundDao == null)
            throw new NoDaoDefinitionException("No Dao Definition is found in Spring Beans for Class:" + entityClass.getCanonicalName());
        return foundDao;
    }


    public static <E extends DomainObject<ID>, ID extends Serializable> DomainObjectDao<E, ID> tryGetDao(Class<E> entityClass) {

        DomainObjectDao<E, ID> dao = null;

        try {
            dao = getDao(entityClass);
        } catch (Throwable ex) {
            dao = null;
        }

        return dao;
    }

    private static boolean isEntityClassMatch(Type providedType, Type requiredType) {

        if (providedType == requiredType) {
            return true;
        }

        if (providedType instanceof Class && requiredType instanceof Class &&
                AbstractDomainObject.class.isAssignableFrom((Class) providedType) &&
                AbstractDomainObject.class.isAssignableFrom((Class) requiredType)) {

            Class providedClass = (Class) providedType;
            Class requiredClass = ((Class) requiredType).getSuperclass();

            while (requiredClass != null && !requiredClass.equals(AbstractDomainObject.class)) {

                if (providedClass == requiredClass) {
                    logger.warn("Could not found Dao Definition for Class [" + ((Class) requiredType).getCanonicalName() + "], use the Dao of super class [" + providedClass
                            .getCanonicalName() + "] instead.");
                    return true;
                }

                requiredClass = requiredClass.getSuperclass();
            }
        }

        return false;
    }

}