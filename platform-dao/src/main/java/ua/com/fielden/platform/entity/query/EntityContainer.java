package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.ProxyFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
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
    private boolean proxy = false;
    private boolean strictProxy = false;

    public EntityContainer(final Class<R> resultType, final ICompanionObjectFinder coFinder) {
        this.resultType = resultType;
        this.coFinder = coFinder;
    }

    public void setProxy() {
        this.proxy = true;
    }

    public void setStrictProxy() {
        this.strictProxy = true;
    }

    private int countAllDataItems() {
        return primitives.size() + entities.size() + composites.size() + collections.size();
    }

    public boolean isEmpty() {
        if (isUnionEntityType(resultType)) {
            for (final EntityContainer<? extends AbstractEntity<?>> entityContainer : entities.values()) {
                if (!entityContainer.isEmpty()) {
                    return false;
                }
            }
            return true;
        }
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

    private <E extends AbstractEntity<?>> Object instantiateStrictProxy(final Class<E> entityType, final Long id, final ProxyCache cache) {
        return cache.getProxy(entityType, id);
    }

    private <E extends AbstractEntity<?>> Object instantiateLazyProxy(final Class<E> entityType, final R owningEntity, final Long id, final String propName) {
        final EntityProxyFactory<?> epf = new EntityProxyFactory<>(entityType);
        final IEntityDao<E> coForProxy = coFinder.find(entityType);
        return epf.create(id, owningEntity, propName, coForProxy, ProxyMode.LAZY);
    }

    public R instantiate(final EntityFactory entFactory, final boolean userViewOnly, final ProxyMode proxyMode, final ProxyCache cache) {
        entity = userViewOnly ? entFactory.newPlainEntity(resultType, getId()) : entFactory.newEntity(resultType, getId());
        entity.beginInitialising();

        final Set<String> proxiedProps = new HashSet<>();

        final boolean unionEntity = isUnionEntityType(resultType);

        for (final Map.Entry<String, Object> primPropEntry : primitives.entrySet()) {
            setPropertyValue(entity, primPropEntry.getKey(), primPropEntry.getValue());
        }

        for (final Map.Entry<String, ValueContainer> compositePropEntry : composites.entrySet()) {
            setPropertyValue(entity, compositePropEntry.getKey(), compositePropEntry.getValue().instantiate());
        }

        for (final Map.Entry<String, EntityContainer<? extends AbstractEntity<?>>> entityEntry : entities.entrySet()) {
            final Object propValue = determinePropValue(entity, entityEntry.getKey(), entityEntry.getValue(), entFactory, userViewOnly, proxyMode, cache);
            if (propValue != null && ProxyFactory.isProxyClass(propValue.getClass())) {
                proxiedProps.add(entityEntry.getKey());
            }
            setPropertyValue(entity, entityEntry.getKey(), propValue);
            if (unionEntity && propValue != null /*&& userViewOnly*/) {
                // FIXME ((AbstractUnionEntity) entity).ensureUnion(entityEntry.getKey());
            }
        }

        for (final Map.Entry<String, CollectionContainer<? extends AbstractEntity<?>>> entityEntry : collections.entrySet()) {
            setPropertyValue(entity, entityEntry.getKey(), entityEntry.getValue().instantiate(entFactory, userViewOnly, proxyMode, cache));
        }

        if (!userViewOnly) {
            EntityUtils.handleMetaProperties(entity, proxiedProps);
        }

        entity.endInitialising();

        return entity;
    }

    private Object determinePropValue(final R owningEntity, final String propName, final EntityContainer<? extends AbstractEntity<?>> entityContainer, final EntityFactory entFactory, final boolean userViewOnly, final ProxyMode proxyMode, final ProxyCache cache) {
        if (entityContainer.proxy) {
            switch (proxyMode) {
            case STRICT:
                return instantiateStrictProxy(entityContainer.resultType, entityContainer.getId(), cache);
            case LAZY:
                return instantiateLazyProxy(entityContainer.resultType, owningEntity, entityContainer.getId(), propName);
            default:
                throw new IllegalStateException("Unknown proxy mode [" + proxyMode + "]");
            }
        } else if (entityContainer.strictProxy) {
            return instantiateStrictProxy(entityContainer.resultType, entityContainer.getId(), cache);
        } else if (entityContainer.isEmpty()) {
            return null;
        } else if (entityContainer.isInstantiated()) {
            return entityContainer.entity;
        } else {
            return entityContainer.instantiate(entFactory, userViewOnly, proxyMode, cache);
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