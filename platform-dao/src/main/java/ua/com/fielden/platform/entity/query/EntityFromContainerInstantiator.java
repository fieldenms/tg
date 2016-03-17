package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.Finder;

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
            return entContainer.isInstrumented() ? entFactory.newEntity(entContainer.getResultType(), entContainer.getId())
                    : entFactory.newPlainEntity(entContainer.getResultType(), entContainer.getId());
        }

        return entContainer.isInstrumented() ? entFactory.newEntity(entContainer.getProxiedResultType(), entContainer.getId())
                : entFactory.newPlainEntity(entContainer.getProxiedResultType(), entContainer.getId());
    }

    public <R extends AbstractEntity<?>> R instantiateFully(final EntityContainer<R> entityContainer, final R justAddedEntity) {
        justAddedEntity.beginInitialising();

        final boolean unionEntity = isUnionEntityType(entityContainer.getResultType());

        for (final Map.Entry<String, Object> primPropEntry : entityContainer.getPrimitives().entrySet()) {
            if (!justAddedEntity.proxiedPropertyNames().contains(primPropEntry.getKey()) && !primPropEntry.getKey().equals(ID)) {
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
                if (unionEntity && propValue != null /*&& lightweight*/) {
                    // FIXME ((AbstractUnionEntity) entity).ensureUnion(entityEntry.getKey());
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
            e.printStackTrace();
            throw new IllegalStateException("Can't set value [" + propValue + "] of type [" + (propValue != null ? propValue.getClass() : "?") + "] for property [" + propName
                    + "] due to:" + e);
        }
    }

    private <R extends AbstractEntity<?>> void setPropertyToField(final R entity, final String propName, final Object propValue, final Class<R> resultType) throws Exception {
        final Field field = Finder.findFieldByName(resultType, propName);
        field.setAccessible(true);
        field.set(entity, propValue);
        field.setAccessible(false);
    }
}