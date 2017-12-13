package org.aggregateframework.spring.repository;

import org.aggregateframework.SystemException;
import org.aggregateframework.dao.AggregateDao;
import org.aggregateframework.dao.AggregateRootDao;
import org.aggregateframework.dao.CollectiveDomainObjectDao;
import org.aggregateframework.dao.DomainObjectDao;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.entity.DomainObject;
import org.aggregateframework.repository.TraversalAggregateRepository;
import org.aggregateframework.spring.context.DaoFactory;
import org.aggregateframework.spring.entity.DaoAwareQuery;
import org.aggregateframework.utils.CollectionUtils;
import org.aggregateframework.utils.DomainObjectUtils;
import org.aggregateframework.utils.ReflectionUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Created by changmingxie on 1/23/15.
 */
public class DaoAwareAggregateRepository<T extends AggregateRoot<ID>, ID extends Serializable> extends TraversalAggregateRepository<T, ID> {

    protected DaoAwareAggregateRepository(Class<T> aggregateType) {
        super(aggregateType);
    }

    @Override
    protected long doCount() {
        AggregateRootDao<T, ID> dao = (AggregateRootDao<T, ID>) DaoFactory.getDao(this.aggregateType);
        return dao.count();
    }

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> int doUpdate(Collection<E> entities) {
        DomainObjectDao<E, I> dao = DaoFactory.getDao((Class) entities.iterator().next().getClass());

        if (dao instanceof CollectiveDomainObjectDao) {
            CollectiveDomainObjectDao<E, I> collectiveDao = (CollectiveDomainObjectDao<E, I>) dao;
            return collectiveDao.updateAll(entities);
        } else {
            int effectedCount = 0;
            for (E entity : entities) {
                effectedCount += dao.update(entity);
            }
            return effectedCount;
        }
    }

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> int doInsert(Collection<E> entities) {

        DomainObjectDao<E, I> dao = DaoFactory.getDao((Class) entities.iterator().next().getClass());

        if (dao instanceof CollectiveDomainObjectDao) {
            CollectiveDomainObjectDao<E, I> collectiveDao = (CollectiveDomainObjectDao<E, I>) dao;
            return collectiveDao.insertAll(entities);
        } else {
            int effectedCount = 0;
            for (E entity : entities) {
                effectedCount += dao.insert(entity);
            }
            return effectedCount;
        }
    }

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> int doDelete(Collection<E> entities) {
        DomainObjectDao<E, I> dao = DaoFactory.getDao((Class) entities.iterator().next().getClass());

        if (dao instanceof CollectiveDomainObjectDao) {
            CollectiveDomainObjectDao<E, I> collectiveDao = (CollectiveDomainObjectDao<E, I>) dao;
            return collectiveDao.deleteAll(entities);
        } else {
            int effectedCount = 0;
            for (E entity : entities) {
                effectedCount += dao.delete(entity);
            }
            return effectedCount;
        }
    }

    @Override
    protected T doFindOneDomainObject(Class<T> entityClass, ID id) {
        DomainObjectDao<T, ID> dao = DaoFactory.getDao(entityClass);
        T entity = dao.findById(id);
        return entity;
    }

    @Override
    protected List<T> doFindAllDomainObjects(Class<T> aggregateType) {
        AggregateRootDao<T, ID> dao = (AggregateRootDao<T, ID>) DaoFactory.getDao(aggregateType);
        List<T> entities = dao.findAll();
        return entities;
    }

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> List<E> doFindAllDomainObjects(Class<E> entityClass, List<I> ids) {
        DomainObjectDao<E, I> dao = DaoFactory.getDao(entityClass);

        if (dao instanceof CollectiveDomainObjectDao) {
            CollectiveDomainObjectDao<E, I> collectiveDao = (CollectiveDomainObjectDao<E, I>) dao;
            return collectiveDao.findByIds(ids);
        } else {

            List<E> entities = new ArrayList<E>();
            for (I id : ids) {
                E entity = dao.findById(id);

                if (entity != null) {
                    entities.add(entity);
                }
            }
            return entities;
        }
    }

    @Override
    protected <I extends Serializable, E extends DomainObject<I>> Map<I, List<DomainObject<Serializable>>> doFindAllOneToManyDomainObjects(Class<E> entityClass, Field oneToManyField, Collection<I> ids) {

        ParameterizedType genericType = (ParameterizedType) oneToManyField.getGenericType();

        Class<DomainObject<Serializable>> oneToManyEntityClass = (Class<DomainObject<Serializable>>) genericType.getActualTypeArguments()[0];

        DomainObjectDao oneToManyComponentDao = (DomainObjectDao) DaoFactory.tryGetDao(oneToManyEntityClass);
        Method fetchedMethodInComponentDao = findAggregateQueryMethodInComponentDao(oneToManyComponentDao, entityClass, oneToManyField);

        List<DomainObject<Serializable>> oneToManyComponents = null;
        try {

            if (Collection.class.isAssignableFrom(fetchedMethodInComponentDao.getParameterTypes()[0])) {
                oneToManyComponents = (List<DomainObject<Serializable>>) fetchedMethodInComponentDao.invoke(oneToManyComponentDao, new ArrayList<Integer>((Collection<Integer>) ids));
            } else {
                oneToManyComponents = new ArrayList<DomainObject<Serializable>>();
                for (I id : ids) {
                    List<DomainObject<Serializable>> components = (List<DomainObject<Serializable>>) fetchedMethodInComponentDao.invoke(oneToManyComponentDao, id);
                    if (!CollectionUtils.isEmpty(components)) {
                        oneToManyComponents.addAll(components);
                    }
                }
            }

        } catch (IllegalAccessException e) {
            throw new SystemException(e);
        } catch (InvocationTargetException e) {
            throw new SystemException(e);
        }

        Field mappedByField = getMappedByField(entityClass, oneToManyField);
        ReflectionUtils.makeAccessible(mappedByField);
        Map<I, List<DomainObject<Serializable>>> identifyOneToManyEntityMap = new LinkedHashMap<I, List<DomainObject<Serializable>>>();

        Map<Serializable, DomainObject<Serializable>> oneToManyEntityMap = new LinkedHashMap<Serializable, DomainObject<Serializable>>();

        for (DomainObject<Serializable> entity : oneToManyComponents) {
            oneToManyEntityMap.put(entity.getId(), entity);
        }

        for (Map.Entry<Serializable, DomainObject<Serializable>> entry : oneToManyEntityMap.entrySet()) {

            I mappedByFieldDomainObjectId = null;

            if (DomainObject.class.isAssignableFrom(mappedByField.getType())) {

                DomainObject<I> mappedByFieldValue = (DomainObject<I>) ReflectionUtils.getField(mappedByField, entry.getValue());
                mappedByFieldDomainObjectId = mappedByFieldValue.getId();
            } else {
                mappedByFieldDomainObjectId = (I) ReflectionUtils.getField(mappedByField, entry.getValue());
            }

            if (!identifyOneToManyEntityMap.containsKey(mappedByFieldDomainObjectId)) {
                identifyOneToManyEntityMap.put(mappedByFieldDomainObjectId, new ArrayList<DomainObject<Serializable>>());
            }
            identifyOneToManyEntityMap.get(mappedByFieldDomainObjectId).add((DomainObject<Serializable>) entry.getValue());
        }

        return identifyOneToManyEntityMap;
    }

    protected <E extends DomainObject<I>, I extends Serializable> Field getMappedByField(Class<E> entityClass, Field oneToManyField) {
        ParameterizedType genericType = (ParameterizedType) oneToManyField.getGenericType();
        Class<DomainObject<Integer>> oneToManyEntityClass = (Class<DomainObject<Integer>>) genericType.getActualTypeArguments()[0];
        return DomainObjectUtils.getFieldByName(oneToManyEntityClass, oneToManyField.getAnnotation(DaoAwareQuery.class).mappedBy());
    }

    private <S extends DomainObject<I>, I extends Serializable> Method findAggregateQueryMethodInComponentDao(DomainObjectDao dao, Class<S> targetEntityClass, Field oneToManyField) {

        Class<?> domainObjectDaoClass = null;

        AggregateDao aggregateDao = AnnotationUtils.findAnnotation(dao.getClass(), AggregateDao.class);

        if (aggregateDao != null) {
            domainObjectDaoClass = dao.getClass();
        } else {

            Class<?>[] daoInterfaces = dao.getClass().getInterfaces();

            for (Class<?> clazz : daoInterfaces) {
                if (DomainObjectDao.class.isAssignableFrom(clazz)) {
                    domainObjectDaoClass = clazz;
                    break;
                }
            }
        }

        DaoAwareQuery daoAwareQuery = oneToManyField.getAnnotation(DaoAwareQuery.class);

        if (daoAwareQuery != null) {

            Method[] methods = domainObjectDaoClass.getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().equals(daoAwareQuery.select())) {
                    return method;
                }
            }
        }

        throw new RuntimeException(String.format("cannot find aggregate query method in component dao:%s, the target entity class is:%s, the field name is:%s", domainObjectDaoClass, targetEntityClass, oneToManyField.getName()));
    }

}
