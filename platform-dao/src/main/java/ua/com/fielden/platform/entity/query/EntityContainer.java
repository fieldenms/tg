package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.ProxyFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.proxy.EntityProxyFactory;
import ua.com.fielden.platform.entity.proxy.ProxyMode;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

public final class EntityContainer<R extends AbstractEntity<?>> {

    private final Class<R> resultType;
    private R entity;
    private final Map<String, Object> primitives = new HashMap<String, Object>();
    private final Map<String, ValueContainer> composites = new HashMap<String, ValueContainer>();
    private final Map<String, EntityContainer<? extends AbstractEntity<?>>> entities = new HashMap<String, EntityContainer<? extends AbstractEntity<?>>>();
    private final Map<String, CollectionContainer<? extends AbstractEntity<?>>> collections = new HashMap<String, CollectionContainer<? extends AbstractEntity<?>>>();
    private final ICompanionObjectFinder coFinder;

    public EntityContainer(final Class<R> resultType, final ICompanionObjectFinder coFinder) {
        this.resultType = resultType;
        this.coFinder = coFinder;
    }

    private int countAllDataItems() {
        return primitives.size() + entities.size() + composites.size() + collections.size();
    }

    public boolean isEmpty() {
        return (countAllDataItems() == 1 && primitives.containsKey(AbstractEntity.ID) && getId() == null) || (isUnionEntityType(resultType) && countAllDataItems() == 0);
    }

    public boolean notYetInitialised() {
        return countAllDataItems() == 1 && getId() != null && !isUnionEntityType(resultType);
    }

    public boolean isInstantiated() {
        return entity != null;
    }

    public Long getId() {
        final Object idObject = primitives.get(AbstractEntity.ID);
        return idObject != null ? new Long(((Number) idObject).longValue())
                : (isUnionEntityType(resultType) ? (entities.values().iterator().hasNext() ? entities.values().iterator().next().getId() : null) : null);
    }

    private Object instantiateProxy(final Class<? extends AbstractEntity<?>> entityType, final R owningEntity, final Long id, final String propName,  final ProxyMode proxyMode) {
        final EntityProxyFactory<?> epf = new EntityProxyFactory<>(entityType);
        return epf.create(id, owningEntity, propName, coFinder.find(entityType), proxyMode);
    }

    public R instantiate(final EntityFactory entFactory, final boolean userViewOnly, final ProxyMode proxyMode) {
        entity = userViewOnly ? entFactory.newPlainEntity(resultType, getId()) : entFactory.newEntity(resultType, getId());
        entity.beginInitialising();
        
        final List<String> proxiedProps = new ArrayList<>(); 
        
        final boolean unionEntity = isUnionEntityType(resultType);

        for (final Map.Entry<String, Object> primPropEntry : primitives.entrySet()) {
            setPropertyValue(entity, primPropEntry.getKey(), primPropEntry.getValue());
        }

        for (final Map.Entry<String, ValueContainer> compositePropEntry : composites.entrySet()) {
            setPropertyValue(entity, compositePropEntry.getKey(), compositePropEntry.getValue().instantiate());
        }

        for (final Map.Entry<String, EntityContainer<? extends AbstractEntity<?>>> entityEntry : entities.entrySet()) {
            final Object propValue = determinePropValue(entity, entityEntry.getKey(), entityEntry.getValue(), entFactory, userViewOnly, proxyMode);
            if (propValue != null && ProxyFactory.isProxyClass(propValue.getClass())) {
                proxiedProps.add(entityEntry.getKey());
            }
            setPropertyValue(entity, entityEntry.getKey(), propValue);
            if (unionEntity && propValue != null /*&& userViewOnly*/) {
                ((AbstractUnionEntity) entity).ensureUnion(entityEntry.getKey());
            }
        }

        for (final Map.Entry<String, CollectionContainer<? extends AbstractEntity<?>>> entityEntry : collections.entrySet()) {
            setPropertyValue(entity, entityEntry.getKey(), entityEntry.getValue().instantiate(entFactory, userViewOnly, proxyMode));
        }

        if (!userViewOnly) {
            EntityUtils.handleMetaProperties(entity, proxiedProps.toArray(new String[]{}));
        }

        entity.endInitialising();

        return entity;
    }

    private Object determinePropValue(final R owningEntity, final String propName, final EntityContainer<? extends AbstractEntity<?>> entityContainer, final EntityFactory entFactory, final boolean userViewOnly,  final ProxyMode proxyMode) {
        if (entityContainer.isEmpty() || entityContainer.notYetInitialised()) {
            return instantiateProxy(entityContainer.resultType, owningEntity, entityContainer.getId(), propName, proxyMode);
        } else if (entityContainer.isInstantiated()) {
            return entityContainer.entity;
        } else {
            return entityContainer.instantiate(entFactory, userViewOnly, proxyMode);
        }
    }

    private void setPropertyValue(final R entity, final String propName, final Object propValue) {
        try {
            if (EntityAggregates.class.equals(resultType) || propValue instanceof Set) {
                entity.set(propName, propValue);
            } else {
                setPropertyToField(entity, propName, propValue);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Can't set value [" + propValue + "] of type [" + (propValue != null ? propValue.getClass() : "?") + "] for property [" + propName
                    + "] due to:" + e);
        }
    }

    private void setPropertyToField(final R entity, final String propName, final Object propValue) throws Exception {
        final Field field = Finder.findFieldByName(resultType, propName);
        field.setAccessible(true);
        field.set(entity, propValue);
        field.setAccessible(false);
    }

    public Class<R> getResultType() {
        return resultType;
    }

    public Map<String, Object> getPrimitives() {
        return primitives;
    }

    public Map<String, ValueContainer> getComposites() {
        return composites;
    }

    public Map<String, EntityContainer<? extends AbstractEntity<?>>> getEntities() {
        return entities;
    }

    public Map<String, CollectionContainer<? extends AbstractEntity<?>>> getCollections() {
        return collections;
    }
}