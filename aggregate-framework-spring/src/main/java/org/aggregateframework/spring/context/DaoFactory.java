package org.aggregateframework.spring.context;

import org.aggregateframework.NoDaoDefinitionException;
import org.aggregateframework.dao.DomainObjectDao;
import org.aggregateframework.entity.AbstractDomainObject;
import org.aggregateframework.entity.DomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author changming.xie
 */
public class DaoFactory {

    private static final Logger logger = LoggerFactory.getLogger(DaoFactory.class);

    private static Map<Class<? extends DomainObject>, DomainObjectDao> daoMap = new ConcurrentHashMap<Class<? extends DomainObject>, DomainObjectDao>();

    public static <E extends DomainObject<ID>, ID extends Serializable> DomainObjectDao<E, ID> getDao(Class<E> entityClass) {

        DomainObjectDao<E, ID> foundDao = null;

        foundDao = daoMap.get(entityClass);

        if (foundDao != null) {
            return foundDao;
        }

        Map<String, DomainObjectDao> daos = SpringObjectFactory.getApplicationContext().getBeansOfType(DomainObjectDao.class);

        for (DomainObjectDao<E, ID> dao : daos.values()) {

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

        throw new NoDaoDefinitionException("No Dao Definition is found in Spring Beans for Class:" + entityClass.getCanonicalName());
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
                    logger.warn("Could not found Dao Definition for Class [" + ((Class) requiredType).getCanonicalName() + "], use the Dao of super class [" + providedClass.getCanonicalName() + "] instead.");
                    return true;
                }

                requiredClass = requiredClass.getSuperclass();
            }
        }

        return false;
    }

}