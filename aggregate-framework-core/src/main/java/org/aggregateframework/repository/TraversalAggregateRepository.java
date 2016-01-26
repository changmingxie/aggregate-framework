package org.aggregateframework.repository;

import org.aggregateframework.OptimisticLockException;
import org.aggregateframework.SystemException;
import org.aggregateframework.context.CollectionUtils;
import org.aggregateframework.context.DomainObjectUtils;
import org.aggregateframework.context.IdentifiedEntityMap;
import org.aggregateframework.context.ReflectionUtils;
import org.aggregateframework.entity.AbstractDomainObject;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.entity.CompositeId;
import org.aggregateframework.entity.DomainObject;
import org.aggregateframework.session.AggregateContext;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * User: changming.xie
 * Date: 14-7-25
 * Time: 下午6:50
 */
public abstract class TraversalAggregateRepository<T extends AggregateRoot<ID>, ID extends Serializable> extends AbstractAggregateRepository<T, ID> {

    protected TraversalAggregateRepository(Class<T> aggregateType) {
        super(aggregateType);
    }

    @Override
    protected Collection<T> doSave(Collection<T> entities) {

        Class idClass = DomainObjectUtils.getIdClass(this.aggregateType);

        List<T> insertEntities = new ArrayList<T>();
        List<T> updateEntities = new ArrayList<T>();

        for (T entity : entities) {

            ensureCompositeIdInitialized(idClass, entity);

            if (entity.isNew()) {
                insertEntities.add(entity);
            } else {
                updateEntities.add(entity);
            }
        }

        if (!CollectionUtils.isEmpty(insertEntities)) {
            AggregateContext aggregateContext = new AggregateContext();
            insertDomainObject(this.aggregateType, insertEntities, aggregateContext);
        }

        if (!CollectionUtils.isEmpty(updateEntities)) {

            List<Pair<T, T>> currentAndOriginalEntityPairs = buildCurrentAndOriginalEntityPairs(updateEntities);

            AggregateContext aggregateContext = new AggregateContext();
            updateDomainObject(this.aggregateType, currentAndOriginalEntityPairs, aggregateContext);
            compareAndSetRootVersion(currentAndOriginalEntityPairs, aggregateContext);
        }

        return entities;
    }

    @Override
    protected void doRemove(Collection<T> entities) {
        AggregateContext aggregateContext = new AggregateContext();
        removeDomainObject(this.aggregateType, entities, aggregateContext);
    }

    @Override
    protected T doFindOne(ID id) {
        return doFindOne(this.aggregateType, id);
    }

    @Override
    protected boolean doExists(ID id) {
        T entity = doFindOneDomainObject(this.aggregateType, id);//instantiateEntity(this.aggregateType, id);
        if (entity != null) {
            return true;
        }
        return false;
    }

    @Override
    protected List<ID> doFindAllIds() {
        List<T> entities = doFindAllDomainObjects(this.aggregateType);

        List<ID> ids = new ArrayList<ID>();

        for (T entity : entities) {
            ids.add(entity.getId());
        }

        return ids;
    }

    @Override
    protected List<T> doFindAll(Collection<ID> ids) {
        IdentifiedEntityMap identifiedEntityMap = new IdentifiedEntityMap();
        List<T> entities = doFindAll(this.aggregateType, ids, identifiedEntityMap);
        return entities;
    }


    protected T fetchAllComponents(T entity) {
        if (entity == null) {
            return null;
        }

        return fetchAllComponents(Arrays.asList(entity)).get(0);
    }

    protected List<T> fetchAllComponents(List<T> entities) {
        IdentifiedEntityMap identifiedEntityMap = new IdentifiedEntityMap();

        for (T entity : entities) {
            Class<T> entityClass = (Class<T>) entity.getClass();
            identifiedEntityMap.put(entityClass, entity.getId(), entity);
        }

        boolean needRecursiveFetched = true;
        fetchAllOneToOneComponents(entities, identifiedEntityMap, needRecursiveFetched);
        fetchAllOneToManyComponents(entities, identifiedEntityMap, needRecursiveFetched);

        for (T entity : entities) {
            entity.clearDomainEvents();
        }

        return entities;
    }


    private <E extends DomainObject<I>, I extends Serializable> E doFindOne(Class<E> entityClass, I id) {

        E entity = null;

        IdentifiedEntityMap identifiedEntityMap = new IdentifiedEntityMap();
        List<I> ids = new ArrayList<I>();
        ids.add(id);
        List<E> entities = doFindAll(entityClass, ids, identifiedEntityMap);

        if (!CollectionUtils.isEmpty(entities)) {
            entity = entities.get(0);
        }

        return entity;
    }

    private <E extends DomainObject<I>, I extends Serializable> List<E> doFindAll(Class<E> entityClass, Collection<I> ids, IdentifiedEntityMap identifiedEntityMap) {

        List<E> alreadyFetchedEntities = new ArrayList<E>();
        List<I> idsNeedFetch = new ArrayList<I>();
        idsNeedFetch.addAll(ids);


        for (I id : ids) {
            if (identifiedEntityMap.containsKey(entityClass, id)) {
                alreadyFetchedEntities.add(identifiedEntityMap.get(entityClass, id));
                idsNeedFetch.remove(id);
            }
        }

        if (!CollectionUtils.isEmpty(idsNeedFetch)) {
            List<E> entities = doFindAllDomainObjects(entityClass, idsNeedFetch);

            for (E entity : entities) {
                identifiedEntityMap.put(entityClass, entity.getId(), entity);
            }

            fetchAllComponents(entities, identifiedEntityMap, true);

            alreadyFetchedEntities.addAll(entities);
        }
        return alreadyFetchedEntities;
    }

    private <E extends DomainObject<I>, I extends Serializable> void insertDomainObject(Class<E> entityClass, List<E> entities, AggregateContext aggregateContext) {

        insertOneToOneAttributes(entities, aggregateContext);

        for (E entity : entities) {
            setCreateTimeOrLastUpdateTime(entity);
        }

        int effectedCount = doInsert(entities);
        if (effectedCount < entities.size()) {
            throw new OptimisticLockException();
        }

        aggregateContext.setAggregateChanged(true);
        aggregateContext.getEntityMap().put(entityClass, entities);

        insertOneToManyAttributes(entities, aggregateContext);
    }

    private <E extends DomainObject<I>, I extends Serializable> void updateDomainObject(Class<E> entityClass, List<Pair<E, E>> currentAndOriginalEntityPairs, AggregateContext aggregateContext) {

        updateOneToOneAttributes(currentAndOriginalEntityPairs, aggregateContext);
        // update entity

        List<E> updateEntities = new ArrayList<E>();

        for (Pair<E, E> pair : currentAndOriginalEntityPairs) {

            E entity = pair.getLeft();
            E originalEntity = pair.getRight();

            if (!DomainObjectUtils.equal(entity, originalEntity)) {
                setCreateTimeOrLastUpdateTime(entity);
                updateEntities.add(entity);
            } else {
                aggregateContext.getEntityMap().put(entityClass, entity);
            }
        }

        if (!CollectionUtils.isEmpty(updateEntities)) {
            int effectedCount = doUpdate(updateEntities);
            if (effectedCount < updateEntities.size()) {
                throw new OptimisticLockException();
            }
            aggregateContext.setAggregateChanged(true);
        }

        aggregateContext.getEntityMap().put(entityClass, updateEntities);

        updateOneToManyAttributes(currentAndOriginalEntityPairs, aggregateContext);
    }

    private <E extends DomainObject<I>, I extends Serializable> void removeDomainObject(Class<E> entityClass, Collection<E> entities, AggregateContext aggregateContext) {

        int effectedCount = doDelete(entities);

        if (effectedCount < entities.size()) {
            throw new OptimisticLockException();
        }

        aggregateContext.getEntityMap().put(entityClass, entities);

        Map<Field, List<DomainObject<Serializable>>> allOneToOneFieldValuesMap = DomainObjectUtils.getOneToOneValues(entities);

        removeFieldDomainObjects(allOneToOneFieldValuesMap, aggregateContext);

        Map<Field, List<DomainObject<Serializable>>> allOneToManyFieldValuesMap = DomainObjectUtils.getOneToManyAttributeValues(entities);

        removeFieldDomainObjects(allOneToManyFieldValuesMap, aggregateContext);
    }

    private <E extends DomainObject<I>, I extends Serializable> List<E> fetchAllComponents(List<E> entities, IdentifiedEntityMap identifiedEntityMap, boolean needRecursiveFetched) {
        fetchAllOneToOneComponents(entities, identifiedEntityMap, needRecursiveFetched);
        fetchAllOneToManyComponents(entities, identifiedEntityMap, needRecursiveFetched);
        return entities;
    }


    private <E extends DomainObject<I>, I extends Serializable> void insertOneToOneAttributes(List<E> entities, AggregateContext aggregateContext) {
        Map<Field, List<DomainObject<Serializable>>> fieldValuesMap = DomainObjectUtils.getOneToOneValues(entities);
        insertFieldDomainObjects(fieldValuesMap, aggregateContext);
    }

    private <E extends DomainObject<I>, I extends Serializable> void insertOneToManyAttributes(List<E> entities, AggregateContext aggregateContext) {

        Map<Field, List<DomainObject<Serializable>>> fieldValuesMap = DomainObjectUtils.getOneToManyAttributeValues(entities);
        insertFieldDomainObjects(fieldValuesMap, aggregateContext);
    }

    private <E extends DomainObject<I>, I extends Serializable> void updateOneToOneAttributes(List<Pair<E, E>> currentAndOriginalEntityPairs, AggregateContext aggregateContext) {

        Map<Field, List<DomainObject<Serializable>>> allNeedInsertFieldValuesMap = new HashMap<Field, List<DomainObject<Serializable>>>();
        Map<Field, List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>> allNeedUpdateFieldValuesMap = new HashMap<Field, List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>>();
        Map<Field, List<DomainObject<Serializable>>> allNeedRemovedFieldValuesMap = new HashMap<Field, List<DomainObject<Serializable>>>();

        for (Pair<E, E> pair : currentAndOriginalEntityPairs) {

            E currentEntity = pair.getLeft();
            E originalEntity = pair.getRight();

            Map<Field, DomainObject<Serializable>> originalFieldValuesMap = DomainObjectUtils.getOneToOneAttributeValues(originalEntity);
            Map<Field, DomainObject<Serializable>> currentFieldValuesMap = DomainObjectUtils.getOneToOneAttributeValues(currentEntity);

            for (Field field : originalFieldValuesMap.keySet()) {

                DomainObject<Serializable> originalValue = originalFieldValuesMap.get(field);
                DomainObject<Serializable> currentValue = currentFieldValuesMap.get(field);


                if (originalValue != null && (currentValue == null || !currentValue.getId().equals(originalValue.getId()))) {


                    if (!allNeedRemovedFieldValuesMap.containsKey(field)) {
                        allNeedRemovedFieldValuesMap.put(field, new ArrayList<DomainObject<Serializable>>());
                    }

                    allNeedRemovedFieldValuesMap.get(field).add(originalValue);

                }

                if (originalValue != null && currentValue != null && currentValue.getId().equals(originalValue.getId())) {

                    if (!allNeedUpdateFieldValuesMap.containsKey(field)) {
                        allNeedUpdateFieldValuesMap.put(field, new ArrayList<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>());
                    }

                    //compare the equals
                    allNeedUpdateFieldValuesMap.get(field).add(new ImmutablePair<DomainObject<Serializable>, DomainObject<Serializable>>(currentValue, originalValue));

                }

                if (currentValue != null && !currentValue.getId().equals(originalValue.getId())) {

                    if (!allNeedInsertFieldValuesMap.containsKey(field)) {
                        allNeedInsertFieldValuesMap.put(field, new ArrayList<DomainObject<Serializable>>());
                    }

                    allNeedInsertFieldValuesMap.get(field).add(currentValue);

                }
            }
        }

        insertUpdateRemoveFieldDomainObjects(aggregateContext, allNeedInsertFieldValuesMap, allNeedUpdateFieldValuesMap, allNeedRemovedFieldValuesMap);
    }

    private <E extends DomainObject<I>, I extends Serializable> void updateOneToManyAttributes(List<Pair<E, E>> currentAndOriginalEntityPairs, AggregateContext aggregateContext) {

        Map<Field, List<DomainObject<Serializable>>> allNeedInsertFieldValuesMap = new HashMap<Field, List<DomainObject<Serializable>>>();
        Map<Field, List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>> allNeedUpdateFieldValuesMap = new HashMap<Field, List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>>();
        Map<Field, List<DomainObject<Serializable>>> allNeedRemoveFieldValuesMap = new HashMap<Field, List<DomainObject<Serializable>>>();

        for (Pair<E, E> pair : currentAndOriginalEntityPairs) {

            E currentEntity = pair.getLeft();
            E originalEntity = pair.getRight();

            Map<Field, Collection<DomainObject<Serializable>>> originalFieldValuesMap = DomainObjectUtils.getOneToManyAttributeValues((Collection) Arrays.asList(originalEntity));

            Map<Field, Collection<DomainObject<Serializable>>> currentFieldValuesMap = DomainObjectUtils.getOneToManyAttributeValues((Collection) Arrays.asList(currentEntity));

            for (Field field : originalFieldValuesMap.keySet()) {

                Collection<DomainObject<Serializable>> originalValues = originalFieldValuesMap.get(field);
                Collection<DomainObject<Serializable>> currentValues = currentFieldValuesMap.get(field);

                Map<Serializable, DomainObject<Serializable>> currentValueMap = new HashMap<Serializable, DomainObject<Serializable>>();
                Map<Serializable, DomainObject<Serializable>> originalValueMap = new HashMap<Serializable, DomainObject<Serializable>>();

                for (DomainObject<Serializable> currentValue : currentValues) {
                    if (!currentValue.isNew()) {
                        currentValueMap.put(currentValue.getId(), currentValue);
                    }
                }

                for (DomainObject<Serializable> originalValue : originalValues) {
                    if (!originalValue.isNew()) {
                        originalValueMap.put(originalValue.getId(), originalValue);
                    }
                }

                List<DomainObject<Serializable>> addedValues = new ArrayList<DomainObject<Serializable>>();
                List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>> updatedAndOriginalValues = new ArrayList<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>();
                List<DomainObject<Serializable>> removedValues = new ArrayList<DomainObject<Serializable>>();

                for (DomainObject<Serializable> currentValue : currentValues) {
                    if (currentValue.isNew() || !originalValueMap.containsKey(currentValue.getId())) {
                        addedValues.add(currentValue);
                    }
                }

                for (DomainObject<Serializable> originalValue : originalValues) {
                    if (!currentValueMap.containsKey(originalValue.getId())) {
                        removedValues.add(originalValue);
                    } else {
                        updatedAndOriginalValues.add(new ImmutablePair<DomainObject<Serializable>, DomainObject<Serializable>>(currentValueMap.get(originalValue.getId()), originalValue));
                    }
                }

                for (DomainObject<Serializable> value : addedValues) {

                    if (!allNeedInsertFieldValuesMap.containsKey(field)) {
                        allNeedInsertFieldValuesMap.put(field, new ArrayList<DomainObject<Serializable>>());
                    }

                    allNeedInsertFieldValuesMap.get(field).add(value);
                }

                for (Pair<DomainObject<Serializable>, DomainObject<Serializable>> updatedAndOriginalValue : updatedAndOriginalValues) {

                    if (!allNeedUpdateFieldValuesMap.containsKey(field)) {
                        allNeedUpdateFieldValuesMap.put(field, new ArrayList<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>());
                    }

                    allNeedUpdateFieldValuesMap.get(field).add(new ImmutablePair<DomainObject<Serializable>, DomainObject<Serializable>>(updatedAndOriginalValue.getLeft(), updatedAndOriginalValue.getRight()));
                }

                for (DomainObject<Serializable> value : removedValues) {

                    if (!allNeedRemoveFieldValuesMap.containsKey(field)) {
                        allNeedRemoveFieldValuesMap.put(field, new ArrayList<DomainObject<Serializable>>());
                    }

                    allNeedRemoveFieldValuesMap.get(field).add(value);
                }
            }
        }

        insertUpdateRemoveFieldDomainObjects(aggregateContext, allNeedInsertFieldValuesMap, allNeedUpdateFieldValuesMap, allNeedRemoveFieldValuesMap);
    }

    private <E extends DomainObject<I>, I extends Serializable> void fetchAllOneToOneComponents(List<E> entities, IdentifiedEntityMap identifiedEntityMap, boolean needRecursiveFetched) {

        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        List<Field> oneToOneFields = DomainObjectUtils.getOneToOneFields(entities.get(0).getClass());

        for (Field field : oneToOneFields) {

            Class<DomainObject<Serializable>> oneToOneComponentClass = (Class<DomainObject<Serializable>>) field.getType();

            Map<Serializable, E> oneToOneComponentIdAndEntityMap = new HashMap<Serializable, E>();

            for (E entity : entities) {
                try {
                    if (field.get(entity) != null) {
                        oneToOneComponentIdAndEntityMap.put(((DomainObject<Serializable>) field.get(entity)).getId(), entity);
                    }
                } catch (Exception ex) {
                    throw new SystemException(ex);
                }
            }

            List<DomainObject<Serializable>> values = doFindAll(oneToOneComponentClass, oneToOneComponentIdAndEntityMap.keySet(), identifiedEntityMap);

            Map<Serializable, DomainObject<Serializable>> valueIdMap = new HashMap<Serializable, DomainObject<Serializable>>();

            for (DomainObject<Serializable> value : values) {
                valueIdMap.put(value.getId(), value);
            }

            for (E entity : entities) {
                try {
                    DomainObject<Serializable> value = ((DomainObject<Serializable>) field.get(entity));
                    if (value != null) {
                        if (identifiedEntityMap.containsKey(oneToOneComponentClass, value.getId())) {
                            field.set(entity, identifiedEntityMap.get(oneToOneComponentClass, value.getId()));
                        } else {
                            field.set(entity, valueIdMap.get(value.getId()));
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new SystemException(e);
                }
            }
        }
    }

    private <E extends DomainObject<I>, I extends Serializable> void fetchAllOneToManyComponents(List<E> entities, IdentifiedEntityMap identifiedEntityMap, boolean needRecursiveFetched) {

        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        Class<E> entityClass = (Class<E>) entities.get(0).getClass();
        List<Field> oneToManyFields = DomainObjectUtils.getOneToManyFields(entityClass);

        Map<I, E> entityIdMap = new HashMap<I, E>();
        for (E entity : entities) {
            entityIdMap.put(entity.getId(), entity);
        }

        for (Field oneToManyField : oneToManyFields) {

            Map<I, List<DomainObject<Serializable>>> oneToManyComponentMaps = doFindAll(entityClass, oneToManyField, entityIdMap.keySet(), identifiedEntityMap);

            //associate the one to many
            for (E entity : entities) {
                try {
                    ReflectionUtils.makeAccessible(oneToManyField);

                    if (oneToManyComponentMaps.get(entity.getId()) == null) {
                        if (oneToManyField.get(entity) != null && oneToManyField.get(entity) instanceof Collection) {
                            ((Collection) oneToManyField.get(entity)).clear();
                        }
                    } else {
                        oneToManyField.set(entity, oneToManyComponentMaps.get(entity.getId()));
                    }
                } catch (IllegalAccessException e) {
                    throw new SystemException(e);
                }
            }
        }
    }


    private void insertUpdateRemoveFieldDomainObjects(AggregateContext aggregateContext, Map<Field, List<DomainObject<Serializable>>> insertFieldValuesMap, Map<Field, List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>> updateFieldValuesMap, Map<Field, List<DomainObject<Serializable>>> removeFieldValuesMap) {

        insertFieldDomainObjects(insertFieldValuesMap, aggregateContext);

        updateFieldDomainObjects(updateFieldValuesMap, aggregateContext);

        removeFieldDomainObjects(removeFieldValuesMap, aggregateContext);
    }

    private void insertFieldDomainObjects(Map<Field, List<DomainObject<Serializable>>> fieldValuesMap, AggregateContext aggregateContext) {

        removeAlreadySavedEntities(fieldValuesMap, aggregateContext);

        for (Map.Entry<Field, List<DomainObject<Serializable>>> keyValuePair : fieldValuesMap.entrySet()) {
            if (!CollectionUtils.isEmpty(keyValuePair.getValue())) {

                insertDomainObject(DomainObjectUtils.getFieldDomainObjectClass(keyValuePair.getKey()), keyValuePair.getValue(), aggregateContext);
            }
        }
    }

    private void updateFieldDomainObjects(Map<Field, List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>> fieldValuesMap, AggregateContext aggregateContext) {

        for (List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>> fieldValues : fieldValuesMap.values()) {

            List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>> alreadySavedEntities = new ArrayList<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>();

            for (Pair<DomainObject<Serializable>, DomainObject<Serializable>> value : fieldValues) {

                if (aggregateContext.getEntityMap().containsKey(value.getLeft().getClass(), value.getLeft().getId())) {
                    alreadySavedEntities.add(value);
                }
            }

            fieldValues.removeAll(alreadySavedEntities);
        }


        for (Map.Entry<Field, List<Pair<DomainObject<Serializable>, DomainObject<Serializable>>>> fieldValues : fieldValuesMap.entrySet()) {
            if (!CollectionUtils.isEmpty(fieldValues.getValue())) {
                updateDomainObject(DomainObjectUtils.getFieldDomainObjectClass(fieldValues.getKey()), fieldValues.getValue(), aggregateContext);
            }
        }
    }

    private void removeFieldDomainObjects(Map<Field, List<DomainObject<Serializable>>> fieldValuesMap, AggregateContext aggregateContext) {

        removeAlreadySavedEntities(fieldValuesMap, aggregateContext);

        for (Map.Entry<Field, List<DomainObject<Serializable>>> fieldValues : fieldValuesMap.entrySet()) {
            if (!CollectionUtils.isEmpty(fieldValues.getValue())) {
                removeDomainObject(DomainObjectUtils.getFieldDomainObjectClass(fieldValues.getKey()), fieldValues.getValue(), aggregateContext);
            }
        }
    }

    private <E extends DomainObject<I>, I extends Serializable> Map<I, List<DomainObject<Serializable>>> doFindAll(Class<E> entityClass, Field oneToManyField, Collection<I> ids, IdentifiedEntityMap identifiedEntityMap) {

        ParameterizedType genericType = (ParameterizedType) oneToManyField.getGenericType();
        Class<DomainObject<Serializable>> oneToManyEntityClass = (Class<DomainObject<Serializable>>) genericType.getActualTypeArguments()[0];

        Map<I, List<DomainObject<Serializable>>> oneToManyComponentMaps = doFindAllOneToManyDomainObjects(entityClass, oneToManyField, ids);

        List<DomainObject<Serializable>> allOneToManyComponents = new ArrayList<DomainObject<Serializable>>();
        for (List<DomainObject<Serializable>> oneToManyComponents : oneToManyComponentMaps.values()) {
            allOneToManyComponents.addAll(oneToManyComponents);
        }

        List<DomainObject<Serializable>> duplicatedFetchedComponents = new ArrayList<DomainObject<Serializable>>();
        List<DomainObject<Serializable>> alreadyFetchedComponents = new ArrayList<DomainObject<Serializable>>();

        replaceComponentsWithFetchedComponents(identifiedEntityMap, oneToManyEntityClass, allOneToManyComponents, duplicatedFetchedComponents, alreadyFetchedComponents);

        allOneToManyComponents.removeAll(duplicatedFetchedComponents);
        allOneToManyComponents.addAll(alreadyFetchedComponents);

        fetchAllComponents(allOneToManyComponents, identifiedEntityMap, true);

        return oneToManyComponentMaps;
    }


    private List<Pair<T, T>> buildCurrentAndOriginalEntityPairs(List<T> updateEntities) {
        List<Pair<T, T>> currentAndOriginalEntityPairs = new ArrayList<Pair<T, T>>();

        Map<ID, T> needFetchIdEntityMap = new HashMap<ID, T>();

        for (T updateEntity : updateEntities) {
            T originalEntity = sessionFactory.requireClientSession().findOriginalCopy(this.aggregateType, updateEntity.getId());

            if (originalEntity == null) {
                needFetchIdEntityMap.put(updateEntity.getId(), updateEntity);
            } else {
                currentAndOriginalEntityPairs.add(new ImmutablePair<T, T>(updateEntity, originalEntity));
            }
        }

        if (!CollectionUtils.isEmpty(needFetchIdEntityMap.keySet())) {
            List<T> originalEntities = doFindAll(needFetchIdEntityMap.keySet());

            for (T originalEntity : originalEntities) {
                currentAndOriginalEntityPairs.add(new ImmutablePair<T, T>(needFetchIdEntityMap.get(originalEntity.getId()), originalEntity));
            }
        }
        return currentAndOriginalEntityPairs;
    }

    private <E extends DomainObject<I>, I extends Serializable> void setCreateTimeOrLastUpdateTime(E entity) {
        if (entity instanceof AbstractDomainObject) {
            AbstractDomainObject abstractDomainObject = (AbstractDomainObject) entity;

            if (abstractDomainObject.getCreateTime() == null) {
                DomainObjectUtils.setField(entity, DomainObjectUtils.CREATE_TIME, new Date());
            }

            DomainObjectUtils.setField(entity, DomainObjectUtils.LAST_UPDATE_TIME, new Date());
        }
    }

    private void ensureCompositeIdInitialized(Class idClass, T entity) {
        if (CompositeId.class.isAssignableFrom(idClass) && entity.getId() == null) {
            try {
                entity.setId((ID) idClass.newInstance());
            } catch (Throwable e) {
                throw new SystemException("new Instance of Composite Id failed. class:" + idClass.getCanonicalName());
            }
        }
    }

    private void removeAlreadySavedEntities(Map<Field, List<DomainObject<Serializable>>> fieldValuesMap, AggregateContext aggregateContext) {
        for (List<DomainObject<Serializable>> fieldValues : fieldValuesMap.values()) {

            List<DomainObject<Serializable>> alreadySavedEntities = new ArrayList<DomainObject<Serializable>>();

            for (DomainObject<Serializable> value : fieldValues) {

                if (aggregateContext.getEntityMap().containsKey(value.getClass(), value.getId())) {
                    alreadySavedEntities.add(value);
                }
            }

            fieldValues.removeAll(alreadySavedEntities);
        }
    }

    private void compareAndSetRootVersion(List<Pair<T, T>> currentAndOriginalEntityPairs, AggregateContext aggregateContext) {

        List<T> updateEntities = new ArrayList<T>();

        for (Pair<T, T> currentAndOriginalEntityPair : currentAndOriginalEntityPairs) {

            T entity = currentAndOriginalEntityPair.getLeft();
            T originalEntity = currentAndOriginalEntityPair.getRight();

            if (DomainObjectUtils.equal(entity, originalEntity)) {
                if (aggregateContext.isAggregateChanged()) {
                    updateEntities.add(entity);
                }
            }
        }

        if (!CollectionUtils.isEmpty(updateEntities)) {
            int effectedCount = doUpdate(updateEntities);
            if (effectedCount < updateEntities.size()) {
                throw new OptimisticLockException();
            }
        }

        for (Pair<T, T> pair : currentAndOriginalEntityPairs) {
            T entity = pair.getLeft();
            DomainObjectUtils.setField(entity, DomainObjectUtils.VERSION, entity.getVersion() + 1L);
        }
    }

    private void replaceComponentsWithFetchedComponents(IdentifiedEntityMap identifiedEntityMap, Class<DomainObject<Serializable>> oneToManyEntityClass, List<DomainObject<Serializable>> allOneToManyComponents, List<DomainObject<Serializable>> duplicatedFetchedComponents, List<DomainObject<Serializable>> alreadyFetchedComponents) {
        for (DomainObject<Serializable> oneToManyComponent : allOneToManyComponents) {
            if (identifiedEntityMap.containsKey(oneToManyEntityClass, oneToManyComponent.getId())) {
                duplicatedFetchedComponents.add(oneToManyComponent);
                alreadyFetchedComponents.add(identifiedEntityMap.get(oneToManyEntityClass, oneToManyComponent.getId()));
            }
        }
    }


    protected abstract <E extends DomainObject<I>, I extends Serializable> int doInsert(Collection<E> entities);

    protected abstract <E extends DomainObject<I>, I extends Serializable> int doUpdate(Collection<E> entities);

    protected abstract <E extends DomainObject<I>, I extends Serializable> int doDelete(Collection<E> entities);

    protected abstract T doFindOneDomainObject(Class<T> aggregateType, ID id);

    protected abstract List<T> doFindAllDomainObjects(Class<T> aggregateType);

    protected abstract <E extends DomainObject<I>, I extends Serializable> List<E> doFindAllDomainObjects(Class<E> entityClass, List<I> ids);

    protected abstract <I extends Serializable, E extends DomainObject<I>> Map<I, List<DomainObject<Serializable>>> doFindAllOneToManyDomainObjects(Class<E> entityClass, Field oneToManyField, Collection<I> ids);
}
