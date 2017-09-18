package org.aggregateframework.ormlite.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import org.aggregateframework.SystemException;
import org.aggregateframework.utils.DomainObjectUtils;
import org.aggregateframework.utils.ReflectionUtils;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.entity.DomainObject;
import org.aggregateframework.repository.TraversalAggregateRepository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.*;

/**
 * User: changming.xie
 * Date: 2014-09-18
 * Time: 19:49
 */
public abstract class AbstractOrmliteAggregateRepository<T extends AggregateRoot<ID>, ID extends Serializable> extends TraversalAggregateRepository<T, ID> {

    protected AbstractOrmliteAggregateRepository(Class<T> aggregateType) {
        super(aggregateType);
    }

    protected abstract Dao getDao(Class clazz);

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> int doInsert(Collection<E> entities) {

        int effectedCount = 0;

        for (E entity : entities) {
            try {
                Dao.CreateOrUpdateStatus status = getDao((Class<E>) entity.getClass()).createOrUpdate(entity);
                effectedCount += status.getNumLinesChanged();
            } catch (SQLException e) {
                throw new SystemException(e);
            }
        }
        return effectedCount;
    }

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> int doUpdate(Collection<E> entities) {

        int effectedCount = 0;
        for (E entity : entities) {
            try {
                Dao.CreateOrUpdateStatus status = getDao((Class<E>) entity.getClass()).createOrUpdate(entity);
                effectedCount += status.getNumLinesChanged();
            } catch (SQLException e) {
                throw new SystemException(e);
            }
        }
        return effectedCount;
    }

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> int doDelete(Collection<E> entities) {

        int effectedCount = 0;

        for (E entity : entities) {
            try {
                effectedCount += getDao((Class<E>) entity.getClass()).delete(entity);
            } catch (SQLException e) {
                throw new SystemException(e);
            }
        }
        return effectedCount;
    }

    @Override
    protected T doFindOneDomainObject(Class<T> aggregateType, ID id) {
        try {
            Dao<T, ID> dao = getDao(aggregateType);
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new SystemException(e);
        }
    }

    @Override
    protected List<T> doFindAllDomainObjects(Class<T> aggregateType) {
        try {
            Dao<T, ID> dao = getDao(aggregateType);
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new SystemException(e);
        }
    }

    @Override
    protected <E extends DomainObject<I>, I extends Serializable> List<E> doFindAllDomainObjects(Class<E> entityClass, List<I> ids) {
        try {
            Dao<E, I> dao = getDao(entityClass);
            Field idField = ReflectionUtils.findField(entityClass, DomainObjectUtils.ID);
            DatabaseField databaseField = idField.getAnnotation(DatabaseField.class);
            List<E> entities = dao.query(dao.queryBuilder().where().in(databaseField.columnName(), ids).prepare());
            return entities;
        } catch (SQLException e) {
            throw new SystemException(e);
        }
    }

    @Override
    protected <I extends Serializable, E extends DomainObject<I>> Map<I, List<DomainObject<Serializable>>> doFindAllOneToManyDomainObjects(Class<E> entityClass, Field oneToManyField, Collection<I> ids) {

        ParameterizedType genericType = (ParameterizedType) oneToManyField.getGenericType();

        ForeignCollectionField foreignCollectionField = oneToManyField.getAnnotation(ForeignCollectionField.class);
        String foreignFieldName = foreignCollectionField.foreignFieldName();

        Class<DomainObject<Serializable>> oneToManyEntityClass = (Class<DomainObject<Serializable>>) genericType.getActualTypeArguments()[0];

        Field foreignField = ReflectionUtils.findField(oneToManyEntityClass, foreignFieldName);
        ReflectionUtils.makeAccessible(foreignField);

        DatabaseField databaseField = foreignField.getAnnotation(DatabaseField.class);

        try {
            Dao oneToManyDao = getDao(oneToManyEntityClass);

            List<DomainObject<Serializable>> oneToManyComponents = (List<DomainObject<Serializable>>) oneToManyDao.query(oneToManyDao.queryBuilder().where().in(databaseField.columnName(), ids).prepare());
            Map<I, List<DomainObject<Serializable>>> identifyOneToManyEntityMap = new LinkedHashMap<I, List<DomainObject<Serializable>>>();

            Map<Serializable, DomainObject<Serializable>> oneToManyEntityMap = new LinkedHashMap<Serializable, DomainObject<Serializable>>();
            for (DomainObject<Serializable> entity : oneToManyComponents) {
                oneToManyEntityMap.put(entity.getId(), entity);
            }

            for (Map.Entry<Serializable, DomainObject<Serializable>> entry : oneToManyEntityMap.entrySet()) {

                I mappedByFieldDomainObjectId = null;

                if (DomainObject.class.isAssignableFrom(foreignField.getType())) {

                    DomainObject<I> mappedByFieldValue = (DomainObject<I>) ReflectionUtils.getField(foreignField, entry.getValue());
                    mappedByFieldDomainObjectId = mappedByFieldValue.getId();
                } else {
                    mappedByFieldDomainObjectId = (I) ReflectionUtils.getField(foreignField, entry.getValue());
                }

                if (!identifyOneToManyEntityMap.containsKey(mappedByFieldDomainObjectId)) {
                    identifyOneToManyEntityMap.put(mappedByFieldDomainObjectId, new ArrayList<DomainObject<Serializable>>());
                }
                identifyOneToManyEntityMap.get(mappedByFieldDomainObjectId).add((DomainObject<Serializable>) entry.getValue());
            }

            return identifyOneToManyEntityMap;

        } catch (SQLException e) {
            throw new SystemException(e);
        }
    }

    @Override
    protected long doCount() {

        try {
            Dao<T, ID> dao = getDao(this.aggregateType);
            return dao.countOf();
        } catch (SQLException e) {
            throw new SystemException(e);
        }
    }
}
