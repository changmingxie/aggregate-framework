package org.aggregateframework.spring.repository;

import org.aggregateframework.SystemException;
import org.aggregateframework.context.CollectionUtils;
import org.aggregateframework.context.DomainObjectUtils;
import org.aggregateframework.context.ReflectionUtils;
import org.aggregateframework.dao.AggregateRootDao;
import org.aggregateframework.dao.DomainObjectDao;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.entity.DomainObject;
import org.aggregateframework.repository.TraversalAggregateRepository;
import org.aggregateframework.spring.context.DaoFactory;
import org.aggregateframework.spring.entity.DaoAwareQuery;

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
    protected <E extends DomainObject<I>, I extends Serializable> int doUpdate(E entity) {
        DomainObjectDao<E, I> dao = DaoFactory.getDao((Class) entity.getClass());
//        entity.setLastUpdateTime(new Date());
        return dao.update(entity);
    }

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> I doInsert(E entity) {
        DomainObjectDao<E, I> dao = DaoFactory.getDao((Class) entity.getClass());
//        entityToInsert.setCreateTime(new Date());
//        entityToInsert.setLastUpdateTime(new Date());
        dao.insert(entity);
        return entity.getId();
    }

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> void doDelete(E entity) {
        DomainObjectDao<E, I> dao = DaoFactory.getDao((Class) entity.getClass());
        dao.delete(entity);
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
        List<E> entities = dao.findByIds(ids);
        return entities;
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
        Map<I, List<DomainObject<Serializable>>> identifyOneToManyEntityMap = new HashMap<I, List<DomainObject<Serializable>>>();

        Map<Serializable, DomainObject<Serializable>> oneToManyEntityMap = new HashMap<Serializable, DomainObject<Serializable>>();

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

        Class<?>[] daoInterfaces = dao.getClass().getInterfaces();

        Class<?> domainObjectDaoClass = null;
        for (Class<?> clazz : daoInterfaces) {
            if (DomainObjectDao.class.isAssignableFrom(clazz)) {
                domainObjectDaoClass = clazz;
                break;
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
