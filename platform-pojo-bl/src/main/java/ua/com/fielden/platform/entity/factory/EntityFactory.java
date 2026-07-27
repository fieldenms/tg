package ua.com.fielden.platform.entity.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;

/// Factory for instantiating entities derived from [AbstractEntity] with AOP/IoC support.
///
@Singleton
public class EntityFactory {

    private final Injector injector;

    @Inject
    EntityFactory(final Injector injector) {
        this.injector = injector;
    }

    /// This factory method should be used for lightweight entity instantiation (not via injector).
    ///
    public static <T extends AbstractEntity<?>> T newPlainEntity(final Class<T> entityClass, final Long id) {
        try {
            final Constructor<T> constructor = entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            final T entity = constructor.newInstance();
            constructor.setAccessible(false);
            setId(entityClass, id, entity);
            return entity;
        } catch (final RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /// This factory method should be used strictly for Hibernate interceptor during entity instantiation.
    ///
    public <T extends AbstractEntity<?>> T newEntity(final Class<T> entityClass, final Long id) {
        try {
            final T entity = injector.getInstance(entityClass);
            setReferenceToThis(entity, this);
            setId(entityClass, id, entity);
            return entity;
        } catch (final RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /// A convenient factory method, which creates entity with <code>null</code> id.
    ///
    public <T extends AbstractEntity<?>> T newEntity(final Class<T> entityClass) {
        return newEntity(entityClass, null);
    }

    /// Invokes protected [AbstractEntity#setEntityFactory(EntityFactory)] method to reference this [EntityFactory] instance.
    ///
    private static void setReferenceToThis(final AbstractEntity<?> entity, final EntityFactory factory) {
        try {
            final Method method = Reflector.getMethod(entity.getType(), "setEntityFactory", EntityFactory.class);
            method.setAccessible(true);
            method.invoke(entity, factory);
            method.setAccessible(false);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /// Instantiates an entity of the specified type with the passed parameters.
    ///
    /// @param <T> entity type
    /// @param <K> type of the entity key property
    ///
    /// @param entityClass entity class
    /// @param id          id value, which can be null if this is a brand-new entity
    /// @param key         key value
    /// @param desc        entity description, which can be null
    ///
    public <T extends AbstractEntity<K>, K extends Comparable> T newEntity(final Class<T> entityClass, final Long id, final K key, final String desc) {
        try {
            final T entity = injector.getInstance(entityClass);
            setReferenceToThis(entity, this);
            setId(entityClass, id, entity);
            setKey(entityClass, key, entity);
            entity.set(DESC, desc);
            return entity;
        } catch (final RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /// Convenient constructor for instantiation of a brand-new entity with no `id` yet assigned.
    ///
    public <T extends AbstractEntity<K>, K extends Comparable> T newEntity(final Class<T> entityClass, final K key, final String desc) {
        return newEntity(entityClass, null, key, desc);
    }

    /// Convenient constructor for instantiation of a brand-new entity with no `id` yet assigned and with no description.
    ///
    public <T extends AbstractEntity<K>, K extends Comparable> T newByKey(final Class<T> entityClass, final K key) {
        return newEntity(entityClass, null, key, null);
    }

    /// Creates entity with [DynamicEntityKey] with specified keys set
    ///
    /// @param keys the key values that correspond to the composite key members of `entityClass`.
    ///
    /// @return created entity with assigned key members
    ///
    public <T extends AbstractEntity<DynamicEntityKey>> T newByKey(final Class<T> entityClass, final Object... keys) {
        try {
            final T entity = injector.getInstance(entityClass); // DynamicEntityKey should be set in default constructor of entity
            setReferenceToThis(entity, this);
            // setting composite key fields
            final List<Field> fieldList = Finder.getKeyMembers(entityClass);
            if (fieldList.size() != keys.length) {
                throw new IllegalArgumentException("Number of key values is " + keys.length + " but should be " + fieldList.size());
            }
            for (int i = 0; i < fieldList.size(); i++) {
                final Field keyField = fieldList.get(i);
                final Object keyValue = keys[i];

                entity.set(keyField.getName(), keyValue);
            }

            return entity;
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /// Convenient constructor for instantiation of an entity without description.
    ///
    public <T extends AbstractEntity<K>, K extends Comparable> T newEntity(final Class<T> entityClass, final Long id, final K key) {
        return newEntity(entityClass, id, key, null);
    }

    /// Convenience method for setting entity id value.
    ///
    private static <T> void setId(final Class<T> entityClass, final Long id, final T entity) throws IllegalAccessException {
        final Field idField = Finder.getFieldByName(entityClass, ID);
        final boolean accessible = idField.isAccessible();
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(accessible);
    }

    /// Convenience method for setting entity key value.
    ///
    private <T, K> void setKey(final Class<T> entityClass, final K key, final T entity) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Method setKey = Reflector.getMethod(entityClass, "setKey", Comparable.class);
        setKey.setAccessible(true);
        setKey.invoke(entity, key);
    }

}
