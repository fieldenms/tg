package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.exceptions.EntityContainerInstantiationException;
import ua.com.fielden.platform.reflection.Finder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;

public class EntityFromContainerInstantiator {
    private final EntityFactory entFactory;
    private final EntityFromContainerInstantiatorCache containerInstantiatorCache;

    public EntityFromContainerInstantiator(final EntityFactory entFactory) {
        this.entFactory = entFactory;
        this.containerInstantiatorCache = new EntityFromContainerInstantiatorCache(this);
    }

    public <R extends AbstractEntity<?>> R instantiate(final EntityContainer<R> entContainer) {
        return containerInstantiatorCache.getEntity(entContainer);
    }

    public <R extends AbstractEntity<?>> R instantiateInitially(final EntityContainer<R> entContainer) {
        if (entContainer.getProxiedResultType() == null) {
            return entContainer.isInstrumented()
                    ? entFactory.newEntity(entContainer.getResultType(), entContainer.getId())
                    : EntityFactory.newPlainEntity(entContainer.getResultType(), entContainer.getId());
        }

        return entContainer.isInstrumented()
                ? entFactory.newEntity(entContainer.getProxiedResultType(), entContainer.getId())
                : EntityFactory.newPlainEntity(entContainer.getProxiedResultType(), entContainer.getId());
    }

    public <R extends AbstractEntity<?>> R instantiateFully(final EntityContainer<R> entityContainer, final R justAddedEntity) {
        justAddedEntity.beginInitialising();

        for (final Map.Entry<String, Object> primPropEntry : entityContainer.getPrimitives().entrySet()) {
            if (!justAddedEntity.proxiedPropertyNames().contains(primPropEntry.getKey()) && !ID.equals(primPropEntry.getKey())) {
                setPropertyValue(justAddedEntity, primPropEntry.getKey(), primPropEntry.getValue(), entityContainer.getResultType());
            }
        }

        for (final Map.Entry<String, ValueContainer> compositePropEntry : entityContainer.getComposites().entrySet()) {
            if (!justAddedEntity.proxiedPropertyNames().contains(compositePropEntry.getKey())) {
                setPropertyValue(justAddedEntity, compositePropEntry.getKey(), instantiate(compositePropEntry.getValue()), entityContainer.getResultType());
            }
        }

        for (final Map.Entry<String, EntityContainer<? extends AbstractEntity<?>>> entityEntry : entityContainer.getEntities().entrySet()) {
            final String key = entityEntry.getKey();
            if (!justAddedEntity.proxiedPropertyNames().contains(key)) {

                final Object propValue = determinePropValue(justAddedEntity, key, entityEntry.getValue());
                setPropertyValue(justAddedEntity, key, propValue, entityContainer.getResultType());

                if (justAddedEntity instanceof AbstractUnionEntity union && propValue != null) {
                    union.ensureUnion(key);
                }
            }
        }

        for (final Map.Entry<String, CollectionContainer<? extends AbstractEntity<?>>> entityEntry : entityContainer.getCollections().entrySet()) {
            if (!justAddedEntity.proxiedPropertyNames().contains(entityEntry.getKey())) {
                setPropertyValue(justAddedEntity, entityEntry.getKey(), instantiate(entityEntry.getValue().getContainers()), entityContainer.getResultType());
            }
        }

        return justAddedEntity;
    }

    private Object instantiate(final ValueContainer valueContainer) {
        return valueContainer.getHibType().instantiate(valueContainer.getPrimitives());
    }

    private <R extends AbstractEntity<?>> Collection<R> instantiate(final List<EntityContainer<R>> containers) {
        final SortedSet<R> result = new TreeSet<>();
        for (final EntityContainer<R> container : containers) {
            if (!container.notYetInitialised()) {
                result.add(instantiate(container));
            }
        }

        return result;
    }

    private <R extends AbstractEntity<?>> Object determinePropValue(final R owningEntity, final String propName, final EntityContainer<? extends AbstractEntity<?>> entityContainer) {
        if (entityContainer.isEmpty()) {
            return null;
        } else {
            return containerInstantiatorCache.getEntity(entityContainer);
        }
    }

    private <R extends AbstractEntity<?>> void setPropertyValue(final R entity, final String propName, final Object propValue, final Class<R> resultType) {
        try {
            if (EntityAggregates.class.equals(resultType) || propValue instanceof Set) {
                entity.set(propName, propValue);
            } else {
                setPropertyToField(entity, propName, propValue, resultType);
            }
        } catch (final Exception e) {
            throw new EntityContainerInstantiationException(
                    format("Cannot assign value of type [%s] to property [%s.%s] of type [%s]. Value: %s.",
                           propValue != null ? propValue.getClass().getTypeName() : "null",
                           resultType.getSimpleName(), propName, determinePropertyType(resultType, propName),
                           propValue));
        }
    }

    private <R extends AbstractEntity<?>> void setPropertyToField(final R entity, final String propName, final Object propValue, final Class<R> resultType) throws Exception {
        accessibleField(resultType, propName).set(entity, propValue);
    }

    /// Fields of properties assigned during instantiation, by entity type and property name.
    ///
    /// Looking up a field ([Finder#findFieldByName]) resolves the property path and creates a new [Field] on every call, which is too costly to repeat for every property of every retrieved entity.
    /// [ClassValue] keeps the cached fields with their entity type, for as long as the type exists.
    ///
    /// The cached fields are made accessible once, and are never handed out, so that their accessibility is not shared with other code.
    ///
    private static final ClassValue<ConcurrentMap<String, Field>> ACCESSIBLE_FIELDS = new ClassValue<>() {
        @Override
        protected ConcurrentMap<String, Field> computeValue(final Class<?> type) {
            return new ConcurrentHashMap<>();
        }
    };

    private static Field accessibleField(final Class<?> type, final String propName) {
        return ACCESSIBLE_FIELDS.get(type).computeIfAbsent(propName, name -> {
            final Field field = Finder.findFieldByName(type, name);
            field.setAccessible(true);
            return field;
        });
    }
}
