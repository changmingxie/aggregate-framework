package org.aggregateframework.repository;

import org.aggregateframework.OptimisticLockException;
import org.aggregateframework.context.CollectionUtils;
import org.aggregateframework.context.DomainObjectUtils;
import org.aggregateframework.context.IdentifiedEntityMap;
import org.aggregateframework.context.ReflectionUtils;
import org.aggregateframework.entity.AbstractAggregateRoot;
import org.aggregateframework.entity.AggregateRoot;
import org.aggregateframework.entity.DomainObject;
import org.aggregateframework.session.AggregateContext;
import org.aggregateframework.entity.AbstractDomainObject;
import org.aggregateframework.SystemException;

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
    protected T doSave(T entity) {
        if (entity.isNew()) {
            AggregateContext aggregateContext = new AggregateContext();
            insertDomainObject(entity, aggregateContext);
        } else {

            T originalEntity = sessionFactory.requireClientSession().findOriginalCopy(this.aggregateType, entity.getId());

            if (originalEntity == null) {
                originalEntity = doFindOne(entity.getId());
            }
            AggregateContext aggregateContext = new AggregateContext();

            updateDomainObject(entity, originalEntity, aggregateContext);

            compareAndSetRootVersion(entity, originalEntity, aggregateContext);
        }
        return entity;
    }


    @Override
    protected void doDelete(T entity) {
        AggregateContext aggregateContext = new AggregateContext();
        removeDomainObject(entity, aggregateContext);
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
        List<T> entities = doFindAll(this.aggregateType, ids, identifiedEntityMap, true);
        return entities;
    }


    protected <E extends DomainObject<I>, I extends Serializable> E doFindOne(Class<E> entityClass, I id) {

        E entity = null;

        IdentifiedEntityMap identifiedEntityMap = new IdentifiedEntityMap();
        List<I> ids = new ArrayList<I>();
        ids.add(id);
        List<E> entities = doFindAll(entityClass, ids, identifiedEntityMap, true);

        if (!CollectionUtils.isEmpty(entities)) {
            entity = entities.get(0);
        }

        return entity;
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
            ((AggregateRoot) entity).clearDomainEvents();
        }

        return entities;
    }


    private void compareAndSetRootVersion(T entity, T originalEntity, AggregateContext aggregateContext) {
        if (DomainObjectUtils.equal(entity, originalEntity)) {
            if (aggregateContext.isAggregateChanged()) {
                int effectedCount = doUpdate(entity);
                if (effectedCount < 1) {
                    throw new OptimisticLockException();
                }
                DomainObjectUtils.setField((AbstractAggregateRoot) entity, DomainObjectUtils.VERSION, ((AbstractAggregateRoot) entity).getVersion() + 1L);
            }
        } else {
            DomainObjectUtils.setField((AbstractAggregateRoot) entity, DomainObjectUtils.VERSION, ((AbstractAggregateRoot) entity).getVersion() + 1L);
        }
    }

    private <E extends DomainObject<I>, I extends Serializable> List<E> doFindAll(Class<E> entityClass, Collection<I> ids, IdentifiedEntityMap identifiedEntityMap, boolean needRecursiveFetched) {

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

    private <E extends DomainObject<I>, I extends Serializable> Map<I, List<DomainObject<Serializable>>> doFindAll(Class<E> entityClass, Field oneToManyField, Collection<I> ids, IdentifiedEntityMap identifiedEntityMap, boolean needRecursiveFetched) {

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

    private <E extends DomainObject<I>, I extends Serializable> List<E> fetchAllComponents(List<E> entities, IdentifiedEntityMap identifiedEntityMap, boolean needRecursiveFetched) {
        fetchAllOneToOneComponents(entities, identifiedEntityMap, needRecursiveFetched);
        fetchAllOneToManyComponents(entities, identifiedEntityMap, needRecursiveFetched);
        return entities;
    }

    private <E extends DomainObject<I>, I extends Serializable> I insertDomainObject(E entity, AggregateContext aggregateContext) {

        insertOneToOneAttributes(entity, aggregateContext);

        if (entity instanceof AbstractDomainObject) {
            DomainObjectUtils.setField(entity, DomainObjectUtils.CREATE_TIME, new Date());
            DomainObjectUtils.setField(entity, DomainObjectUtils.LAST_UPDATE_TIME, new Date());
        }

        I id = doInsert(entity);
        aggregateContext.setAggregateChanged(true);
        aggregateContext.getEntityMap().put((Class<E>) entity.getClass(), id, entity);

        insertOneToManyAttributes(entity, aggregateContext);

        return id;
    }

    public <E extends DomainObject<I>, I extends Serializable> void updateDomainObject(E entity, E originalEntity, AggregateContext aggregateContext) {
        updateOneToOneAttributes(entity, originalEntity, aggregateContext);
        // update entity
        if (!DomainObjectUtils.equal(entity, originalEntity)) {
            if (entity instanceof AbstractDomainObject) {
                if (entity.getCreateTime() == null) {
                    DomainObjectUtils.setField(entity, DomainObjectUtils.CREATE_TIME, new Date());
                }
                DomainObjectUtils.setField(entity, DomainObjectUtils.LAST_UPDATE_TIME, new Date());
            }
            int effectedCount = doUpdate(entity);
            if (effectedCount < 1) {
                throw new OptimisticLockException();
            }
            aggregateContext.setAggregateChanged(true);
        }

        aggregateContext.getEntityMap().put((Class<E>) entity.getClass(), entity.getId(), entity);

        updateOneToManyAttributes(entity, originalEntity, aggregateContext);
    }

    public <E extends DomainObject<I>, I extends Serializable> void removeDomainObject(E originalEntity, AggregateContext aggregateContext) {

        doDelete(originalEntity);
        aggregateContext.getEntityMap().put((Class<E>) originalEntity.getClass(), originalEntity.getId(), originalEntity);

        List<DomainObject<Serializable>> allOneToOneAttributeValues = DomainObjectUtils.getOneToOneValues(originalEntity);
        for (DomainObject<Serializable> value : allOneToOneAttributeValues) {
            if (!aggregateContext.getEntityMap().containsKey(value.getClass(), value.getId())) {
                removeDomainObject(value, aggregateContext);
            }
        }

        Collection<Collection<DomainObject<Serializable>>> allOneToManyAttributeValues = DomainObjectUtils
                .getOneToManyValues(originalEntity);

        for (Collection<DomainObject<Serializable>> attributeValues : allOneToManyAttributeValues) {
            for (DomainObject<Serializable> value : attributeValues) {

                if (!aggregateContext.getEntityMap().containsKey(value.getClass(), value.getId())) {
                    removeDomainObject(value, aggregateContext);
                }
            }
        }
    }

    private <E extends DomainObject<I>, I extends Serializable> void insertOneToOneAttributes(E entity, AggregateContext aggregateContext) {
        List<DomainObject<Serializable>> allOneToOneAttributeValues = DomainObjectUtils.getOneToOneValues(entity);

        for (DomainObject<Serializable> value : allOneToOneAttributeValues) {

            if (!aggregateContext.getEntityMap().containsKey((Class<DomainObject<Serializable>>) value.getClass(), value.getId())) {
                insertDomainObject(value, aggregateContext);
            }
        }
    }

    private <E extends DomainObject<I>, I extends Serializable> void insertOneToManyAttributes(E entity, AggregateContext aggregateContext) {

        Map<Field, Collection<DomainObject<Serializable>>> attributes = DomainObjectUtils.getOneToManyAttributeValues(entity);

        for (Map.Entry<Field, Collection<DomainObject<Serializable>>> attribute : attributes.entrySet()) {

            if (attribute.getValue().size() > 0) {

                for (DomainObject<Serializable> value : attribute.getValue()) {
                    if (!aggregateContext.getEntityMap().containsKey((Class<DomainObject<Serializable>>) value.getClass(), value.getId())) {
                        insertDomainObject(value, aggregateContext);
                    }
                }
            }
        }
    }

    private <E extends DomainObject<I>, I extends Serializable> void updateOneToOneAttributes(E entity, E originalEntity, AggregateContext aggregateContext) {

        if (originalEntity == null) {
            return;
        }

        Map<Field, DomainObject<Serializable>> orignalAttributes = DomainObjectUtils
                .getOneToOneAttributeValues(originalEntity);
        Map<Field, DomainObject<Serializable>> newAttributes = DomainObjectUtils.getOneToOneAttributeValues(entity);

        for (Field field : orignalAttributes.keySet()) {

            DomainObject<Serializable> orignalValue = orignalAttributes.get(field);
            DomainObject<Serializable> newValue = newAttributes.get(field);

            if (newValue == null || newValue.isNew()) {
                if (orignalValue != null && !aggregateContext.getEntityMap().containsKey((Class<DomainObject<Serializable>>) orignalValue.getClass(), orignalValue.getId())) {
                    removeDomainObject(orignalValue, aggregateContext);
                }
            }

            if (newValue != null) {
                if (newValue.isNew()) {
                    insertDomainObject(newValue, aggregateContext);
                } else {
                    if (!aggregateContext.getEntityMap().containsKey((Class<DomainObject<Serializable>>) newValue.getClass(), newValue.getId())) {
                        updateDomainObject(newValue, orignalValue, aggregateContext);
                    }
                }
            }
        }
    }

    private <E extends DomainObject<I>, I extends Serializable> void updateOneToManyAttributes(E entity, E originalEntity, AggregateContext aggregateContext) {

        if (originalEntity == null) {
            return;
        }

        Map<Field, Collection<DomainObject<Serializable>>> originalAttributes = DomainObjectUtils
                .getOneToManyAttributeValues(originalEntity);

        Map<Field, Collection<DomainObject<Serializable>>> newAttributes = DomainObjectUtils.getOneToManyAttributeValues(entity);

        for (Field field : originalAttributes.keySet()) {
            Collection<DomainObject<Serializable>> originalValues = originalAttributes.get(field);
            Collection<DomainObject<Serializable>> newValues = newAttributes.get(field);

            List<DomainObject<Serializable>> addedValues = new ArrayList<DomainObject<Serializable>>();
            List<DomainObject<Serializable>> updatedValues = new ArrayList<DomainObject<Serializable>>();
            List<DomainObject<Serializable>> removedValues = new ArrayList<DomainObject<Serializable>>();

            for (DomainObject<Serializable> value : newValues) {

                if (value.isNew()) {
                    addedValues.add(value);
                } else {
                    updatedValues.add(value);
                }
            }

            List<DomainObject<Serializable>> originalValueCopies = new ArrayList<DomainObject<Serializable>>();
            originalValueCopies.addAll(originalValues);

            removedValues.addAll(CollectionUtils.subtract(originalValueCopies, updatedValues,
                    new Comparator<DomainObject<Serializable>>() {
                        @Override
                        public int compare(DomainObject<Serializable> o1, DomainObject<Serializable> o2) {
                            return o1.getId().equals(o2.getId()) ? 0 : -1;
                        }

                    }));

            for (DomainObject<Serializable> value : addedValues) {
                if (!aggregateContext.getEntityMap().containsKey((Class<DomainObject<Serializable>>) value.getClass(), value.getId())) {
                    insertDomainObject(value, aggregateContext);

                }
            }


            Map<Serializable, DomainObject<Serializable>> originalEntityMap = new HashMap<Serializable, DomainObject<Serializable>>();

            for (DomainObject<Serializable> originalValue : originalValues) {
                originalEntityMap.put(originalValue.getId(), originalValue);
            }

            for (DomainObject<Serializable> value : updatedValues) {

                if (!aggregateContext.getEntityMap().containsKey((Class<DomainObject<Serializable>>) value.getClass(), value.getId())) {
                    updateDomainObject(value, originalEntityMap.get(value.getId()), aggregateContext);
                }
            }

            for (DomainObject<Serializable> value : removedValues) {
                if (!aggregateContext.getEntityMap().containsKey((Class<DomainObject<Serializable>>) value.getClass(), value.getId())) {
                    removeDomainObject(value, aggregateContext);
                }
            }
        }
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

            List<DomainObject<Serializable>> values = doFindAll(oneToOneComponentClass, oneToOneComponentIdAndEntityMap.keySet(), identifiedEntityMap, true);

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

            Map<I, List<DomainObject<Serializable>>> oneToManyComponentMaps = doFindAll(entityClass, oneToManyField, entityIdMap.keySet(), identifiedEntityMap, needRecursiveFetched);

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

    private void replaceComponentsWithFetchedComponents(IdentifiedEntityMap identifiedEntityMap, Class<DomainObject<Serializable>> oneToManyEntityClass, List<DomainObject<Serializable>> allOneToManyComponents, List<DomainObject<Serializable>> duplicatedFetchedComponents, List<DomainObject<Serializable>> alreadyFetchedComponents) {
        for (DomainObject<Serializable> oneToManyComponent : allOneToManyComponents) {
            if (identifiedEntityMap.containsKey(oneToManyEntityClass, oneToManyComponent.getId())) {
                duplicatedFetchedComponents.add(oneToManyComponent);
                alreadyFetchedComponents.add(identifiedEntityMap.get(oneToManyEntityClass, oneToManyComponent.getId()));
            }
        }
    }

    protected abstract <E extends DomainObject<I>, I extends Serializable> I doInsert(E entity);

    protected abstract <E extends DomainObject<I>, I extends Serializable> int doUpdate(E entity);

    protected abstract <E extends DomainObject<I>, I extends Serializable> void doDelete(E entity);

    protected abstract T doFindOneDomainObject(Class<T> aggregateType, ID id);

    protected abstract List<T> doFindAllDomainObjects(Class<T> aggregateType);

    protected abstract <E extends DomainObject<I>, I extends Serializable> List<E> doFindAllDomainObjects(Class<E> entityClass, List<I> ids);

    protected abstract <I extends Serializable, E extends DomainObject<I>> Map<I, List<DomainObject<Serializable>>> doFindAllOneToManyDomainObjects(Class<E> entityClass, Field oneToManyField, Collection<I> ids);

//    protected abstract <E extends DomainObject<I>, I extends Serializable> Map<I, Map<String, Object>> findPropertyValues(Class<E> entityClass, Collection<I> id);
//
//    protected abstract Map<ID, Map<String, Object>> findAllAggregateRootPropertyValues();
//
//    protected abstract <E extends DomainObject<I>, I extends Serializable> Map<I, List<Map<String, Object>>> findOneToManyPropertyValues(Class<E> entityClass, String oneToManyProperty, Collection<I> ids);

}
