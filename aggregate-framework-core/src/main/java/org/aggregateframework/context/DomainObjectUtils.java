/**
 *
 */
package org.aggregateframework.context;


import org.aggregateframework.SystemException;
import org.aggregateframework.entity.DomainObject;
import org.aggregateframework.entity.Transient;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author changming.xie
 */
public class DomainObjectUtils {

    public static final String ID = "id";
    public static final String VERSION = "version";
    public static final String IS_DELETED = "isDeleted";
    public static final String CREATE_TIME = "createTime";
    public static final String LAST_UPDATE_TIME = "lastUpdateTime";

    private static Map<Class<? extends DomainObject>, List<Field>> oneToOneFieldMap = new ConcurrentHashMap<Class<? extends DomainObject>, List<Field>>();

    private static Map<Class<? extends DomainObject>, List<Field>> oneToManyFieldMap = new ConcurrentHashMap<Class<? extends DomainObject>, List<Field>>();


    public static Class getIdClass(Class entityClass) {

        Class currentClass = entityClass;

        while (currentClass != null && !currentClass.equals(Object.class)) {

            Type type = currentClass.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                Type idType = null;
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getActualTypeArguments().length > 0) {
                    idType = parameterizedType.getActualTypeArguments()[0];

                    if (idType instanceof Class) {
                        return (Class) idType;
                    }
                }
                break;
            }

            currentClass = currentClass.getSuperclass();
        }

        return null;
    }

    private static boolean isTransientField(Field field) {
        Annotation[] annotations = field.getAnnotations();

        boolean isTransient = false;
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (ReflectionUtils.getSimpleClassName(annotation.annotationType().getName())
                        .equals(ReflectionUtils.getSimpleClassName(Transient.class.getName()))) {
                    isTransient = true;
                    break;
                }
            }
        }
        return isTransient;
    }

    public static void setField(DomainObject entity, String fieldName, Object value) {
        Field idField = ReflectionUtils.findField(entity.getClass(), fieldName);
        ReflectionUtils.makeAccessible(idField);
        ReflectionUtils.setField(idField, entity, value);
    }

    public static <E extends DomainObject<I>, I extends Serializable> Collection<Collection<DomainObject<Serializable>>> getOneToManyValues(E entity) {

        Collection<Collection<DomainObject<Serializable>>> attributeValues = new ArrayList<Collection<DomainObject<Serializable>>>(getOneToManyAttributeValues(entity).values());

        attributeValues.removeAll(Collections.singleton(null));

        return attributeValues;
    }

    public static <E extends DomainObject<I>, I extends Serializable> List<DomainObject<Serializable>> getOneToOneValues(E entity) {

        List<DomainObject<Serializable>> attributeValues = new ArrayList<DomainObject<Serializable>>(DomainObjectUtils
                .getOneToOneAttributeValues(entity).values());

        attributeValues.removeAll(Collections.singleton(null));

        return attributeValues;
    }

    public static <E extends DomainObject<I>, I extends Serializable> Map<Field, Collection<DomainObject<Serializable>>> getOneToManyAttributeValues(
            E entity) {

        Map<Field, Collection<DomainObject<Serializable>>> attributeValues = new HashMap<Field, Collection<DomainObject<Serializable>>>();

        List<Field> oneToManyFields = getOneToManyFields(entity.getClass());

        for (Field field : oneToManyFields) {

            Collection<DomainObject<Serializable>> values;
            try {
                values = (Collection<DomainObject<Serializable>>) field.get(entity);
                attributeValues.put(field, values);
            } catch (Exception e) {
                throw new SystemException(e);
            }

        }
        return attributeValues;
    }

    public static <E extends DomainObject<I>, I extends Serializable> Map<Field, DomainObject<Serializable>> getOneToOneAttributeValues(
            E entity) {

        Map<Field, DomainObject<Serializable>> attributes = new HashMap<Field, DomainObject<Serializable>>();

        List<Field> oneToOneFields = getOneToOneFields(entity.getClass());

        for (Field field : oneToOneFields) {

            DomainObject<Serializable> attributeValue;
            try {
                attributeValue = (DomainObject<Serializable>) field.get(entity);
            } catch (Exception e) {
                throw new SystemException(e);
            }
            attributes.put(field, attributeValue);
        }

        return attributes;
    }

    public static <E extends DomainObject<I>, I extends Serializable> List<Field> getOneToManyFields(Class<E> entityClass) {

        List<Field> fields = oneToManyFieldMap.get(entityClass);

        if (fields != null) {
            return fields;
        }

        fields = new ArrayList<Field>();

        Class currentEntityClass = entityClass;

        while (DomainObject.class.isAssignableFrom(currentEntityClass)) {
            for (Field field : currentEntityClass.getDeclaredFields()) {

                field.setAccessible(true);

                if (isTransientField(field)) {
                    continue;
                }

                Type declaringType = field.getGenericType();


                if (declaringType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) declaringType;

                    if (paramType.getRawType() instanceof Class
                            && Collection.class.isAssignableFrom((Class) paramType.getRawType())
                            && paramType.getActualTypeArguments().length > 0) {
                        Type entityType = paramType.getActualTypeArguments()[0];

                        if (entityType instanceof Class
                                && DomainObject.class.isAssignableFrom((Class) entityType)) {
                            fields.add(field);
                        }
                    }
                }
            }

            currentEntityClass = currentEntityClass.getSuperclass();
        }

        oneToManyFieldMap.put(entityClass, fields);

        return fields;
    }

    public static <E extends DomainObject<I>, I extends Serializable> List<Field> getOneToOneFields(Class<E> entityClass) {

        List<Field> fields = oneToOneFieldMap.get(entityClass);

        if (fields != null) {
            return fields;
        }

        fields = new ArrayList<Field>();

        Class currentEntityClass = entityClass;

        while (DomainObject.class.isAssignableFrom(currentEntityClass)) {
            for (Field field : currentEntityClass.getDeclaredFields()) {

                Type declaringType = field.getGenericType();
                field.setAccessible(true);
                if (declaringType instanceof Class) {
                    if (!ReflectionUtils.isJdkPrimitiveType(((Class) declaringType))
                            && DomainObject.class.isAssignableFrom((Class) declaringType)) {
                        fields.add(field);
                    }
                }
            }
            currentEntityClass = currentEntityClass.getSuperclass();
        }

        oneToOneFieldMap.put(entityClass, fields);

        return fields;
    }

    public static <E extends DomainObject<I>, I extends Serializable> Field getFieldByName(Class<E> entityClass, String fieldName) {

        Class currentEntityClass = entityClass;

        while (DomainObject.class.isAssignableFrom(currentEntityClass)) {
            for (Field field : currentEntityClass.getDeclaredFields()) {

                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }

            currentEntityClass = currentEntityClass.getSuperclass();
        }

        return null;
    }

    public static <E extends DomainObject<I>, I extends Serializable> E instantiateDomainObject(Class<E> entityClass, Map<String, Object> propertyValues) {

        E entity = (E) ObjectUtils.instantiateClass(entityClass);

        for (Map.Entry<String, Object> entry : propertyValues.entrySet()) {

            if (entry.getKey().indexOf('.') > 0) {
                String[] properties = entry.getKey().split("\\.");
                Class currentEntityClass = entityClass;
                Object currentEntity = entity;

                Field field = ReflectionUtils.findField(currentEntityClass, properties[0]);
                if (field != null) {
                    ReflectionUtils.makeAccessible(field);
                    try {
                        DomainObject domainObject = (DomainObject) ObjectUtils.instantiateClass(field.getType());
                        DomainObjectUtils.setField((DomainObject) domainObject, DomainObjectUtils.ID, entry.getValue());
                        field.set(currentEntity, domainObject);
                    } catch (IllegalAccessException e) {
                        throw new SystemException(e);
                    }
                }
            } else {
                Field field = ReflectionUtils.findField(entityClass, entry.getKey());
                if (field != null) {
                    ReflectionUtils.makeAccessible(field);
                    try {
                        field.set(entity, entry.getValue());
                    } catch (IllegalAccessException e) {
                        throw new SystemException(e);
                    }
                }
            }
        }

        return entity;

    }

    public static <E extends DomainObject<I>, I extends Serializable> boolean equal(E entity, E originalEntity) {

        if (entity == null || originalEntity == null) {
            return false;
        }

        if (!entity.getClass().equals(originalEntity.getClass())) {
            return false;
        }


        List<Field> oneToOneFields = getOneToOneFields(entity.getClass());
        List<Field> oneToManyFields = getOneToManyFields(entity.getClass());

        final Set<Field> excludeFields = new HashSet<Field>();

        excludeFields.addAll(oneToOneFields);
        excludeFields.addAll(oneToManyFields);

        final List<Field> comparedFields = new ArrayList<Field>();
        ReflectionUtils.doWithFields(entity.getClass(), new ReflectionUtils.FieldCallback() {
                    @Override
                    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                        comparedFields.add(field);
                    }
                }, new ReflectionUtils.FieldFilter() {
                    @Override
                    public boolean matches(Field field) {

                        if (excludeFields.contains(field)) {
                            return false;
                        } else if (field.getAnnotation(Transient.class) != null) {
                            return false;
                        } else {
                            return true;
                        }


                    }
                }
        );

        for (Field field : comparedFields) {
            ReflectionUtils.makeAccessible(field);

            Object value = ReflectionUtils.getField(field, entity);
            Object originalValue = ReflectionUtils.getField(field, originalEntity);

            if (!ObjectUtils.nullSafeEquals(value, originalValue)) {
                return false;
            }
        }

        return true;
    }
}
