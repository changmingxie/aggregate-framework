/**
 *
 */
package org.aggregateframework.utils;


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
    public static final String IS_NEW = "isNew";
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
        Field field = ReflectionUtils.findField(entity.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, entity, value);
    }

    public static Object getFieldValue(DomainObject entity, String fieldName) {
        Field field = ReflectionUtils.findField(entity.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field,entity);
    }

    public static <E extends DomainObject<I>, I extends Serializable> Map<Field, List<DomainObject<Serializable>>> getOneToOneValues(Collection<E> entities) {

        Map<Field, List<DomainObject<Serializable>>> allOneToOneFieldValuesMap = new LinkedHashMap<Field, List<DomainObject<Serializable>>>();

        for (E entity : entities) {

            Map<Field, DomainObject<Serializable>> fieldValueMap = DomainObjectUtils.getOneToOneAttributeValues(entity);

            for (Map.Entry<Field, DomainObject<Serializable>> keyValuePair : fieldValueMap.entrySet()) {
                if (!allOneToOneFieldValuesMap.containsKey(keyValuePair.getKey())) {
                    allOneToOneFieldValuesMap.put(keyValuePair.getKey(), new ArrayList<DomainObject<Serializable>>());
                }

                if (keyValuePair.getValue() != null) {
                    allOneToOneFieldValuesMap.get(keyValuePair.getKey()).add(keyValuePair.getValue());
                }
            }
        }

        return allOneToOneFieldValuesMap;
    }

    public static <E extends DomainObject<I>, I extends Serializable> Map<Field, List<DomainObject<Serializable>>> getOneToManyAttributeValues(
            Collection<E> entities) {

        Map<Field, List<DomainObject<Serializable>>> attributeValues = new LinkedHashMap<Field, List<DomainObject<Serializable>>>();

        for (E entity : entities) {

            List<Field> oneToManyFields = getOneToManyFields(entity.getClass());
            for (Field field : oneToManyFields) {
                Collection<DomainObject<Serializable>> values;
                try {
                    values = (Collection<DomainObject<Serializable>>) field.get(entity);

                    if (!attributeValues.containsKey(field)) {
                        attributeValues.put(field, new ArrayList<DomainObject<Serializable>>());
                    }

                    if (!CollectionUtils.isEmpty(values)) {
                        attributeValues.get(field).addAll(values);
                    }
                    
                } catch (Exception e) {
                    throw new SystemException(e);
                }
            }

        }
        return attributeValues;
    }

    public static <E extends DomainObject<I>, I extends Serializable> Map<Field, DomainObject<Serializable>> getOneToOneAttributeValues(
            E entity) {

        Map<Field, DomainObject<Serializable>> attributes = new LinkedHashMap<Field, DomainObject<Serializable>>();

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

    public static Class<DomainObject<Serializable>> getFieldDomainObjectClass(Field field) {

        field.setAccessible(true);

        Type declaringType = field.getGenericType();

        if (declaringType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) declaringType;

            if (paramType.getRawType() instanceof Class
                    && Collection.class.isAssignableFrom((Class) paramType.getRawType())
                    && paramType.getActualTypeArguments().length > 0) {
                Type entityType = paramType.getActualTypeArguments()[0];

                if (entityType instanceof Class
                        && DomainObject.class.isAssignableFrom((Class) entityType)) {
                    return (Class<DomainObject<Serializable>>) entityType;
                }
            }
        } else if (declaringType instanceof Class) {
            if (!ReflectionUtils.isJdkPrimitiveType(((Class) declaringType))
                    && DomainObject.class.isAssignableFrom((Class) declaringType)) {
                return (Class<DomainObject<Serializable>>) declaringType;
            }
        }

        return null;
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
