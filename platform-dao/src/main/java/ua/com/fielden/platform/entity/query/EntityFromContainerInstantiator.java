package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javassist.util.proxy.ProxyFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.proxy.EntityProxyFactory;
import ua.com.fielden.platform.entity.proxy.ProxyMode;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

public class EntityFromContainerInstantiator {
    private final EntityFactory entFactory;
    private final ProxyMode proxyMode;
    private final ProxyCache cache;
    private final boolean lightweight;
    private final ICompanionObjectFinder coFinder;
    private final EntityFromContainerInstantiatorCache containerInstantiatorCache;
    
    public EntityFromContainerInstantiator(final EntityFactory entFactory, final boolean lightweight,  final ProxyMode proxyMode, final ProxyCache cache, final ICompanionObjectFinder coFinder) {
        super();
        this.entFactory = entFactory;
        this.proxyMode = proxyMode;
        this.cache = cache;
        this.lightweight = lightweight;
        this.coFinder = coFinder;
        this.containerInstantiatorCache = new EntityFromContainerInstantiatorCache(this);
    }

    public <R extends AbstractEntity<?>> R instantiate(final EntityContainer<R> entContainer) {
        return instantiateFully(entContainer, instantiateInitially(entContainer));
    }

    public <R extends AbstractEntity<?>> R instantiateInitially(final EntityContainer<R> entContainer) {
        return lightweight ? entFactory.newPlainEntity(entContainer.getResultType(), entContainer.getId()) : entFactory.newEntity(entContainer.getResultType(), entContainer.getId());
    }
    
    public <R extends AbstractEntity<?>> R instantiateFully(final EntityContainer<R> entityContainer, final R justAddedEntity) {
        justAddedEntity.beginInitialising();

        final Set<String> proxiedProps = new HashSet<>();

        final boolean unionEntity = isUnionEntityType(entityContainer.getResultType());

        for (final Map.Entry<String, Object> primPropEntry : entityContainer.getPrimitives().entrySet()) {
            setPropertyValue(justAddedEntity, primPropEntry.getKey(), primPropEntry.getValue(), entityContainer.getResultType());
        }

        for (final Map.Entry<String, ValueContainer> compositePropEntry : entityContainer.getComposites().entrySet()) {
            setPropertyValue(justAddedEntity, compositePropEntry.getKey(), instantiate(compositePropEntry.getValue()), entityContainer.getResultType());
        }

        for (final Map.Entry<String, EntityContainer<? extends AbstractEntity<?>>> entityEntry : entityContainer.getEntities().entrySet()) {
            final Object propValue = determinePropValue(justAddedEntity, entityEntry.getKey(), entityEntry.getValue());
            if (propValue != null && ProxyFactory.isProxyClass(propValue.getClass())) {
                proxiedProps.add(entityEntry.getKey());
            }
            setPropertyValue(justAddedEntity, entityEntry.getKey(), propValue, entityContainer.getResultType());
            if (unionEntity && propValue != null /*&& lightweight*/) {
                // FIXME ((AbstractUnionEntity) entity).ensureUnion(entityEntry.getKey());
            }
        }

        for (final Map.Entry<String, CollectionContainer<? extends AbstractEntity<?>>> entityEntry : entityContainer.getCollections().entrySet()) {
            setPropertyValue(justAddedEntity, entityEntry.getKey(), instantiate(entityEntry.getValue().getContainers()), entityContainer.getResultType());
        }

        if (!lightweight) {
            EntityUtils.handleMetaProperties(justAddedEntity, proxiedProps);
        }

        justAddedEntity.endInitialising();

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
    
    private <E extends AbstractEntity<?>> Object instantiateStrictProxy(final Class<E> entityType, final Long id, final ProxyCache cache) {
        return cache.getProxy(entityType, id);
    }

    private <R extends AbstractEntity<?>, E extends AbstractEntity<?>> Object instantiateLazyProxy(final Class<E> entityType, final R owningEntity, final Long id, final String propName) {
        final EntityProxyFactory<?> epf = new EntityProxyFactory<>(entityType);
        final IEntityDao<E> coForProxy = coFinder.find(entityType);
        return epf.create(id, owningEntity, propName, coForProxy, ProxyMode.LAZY);
    }
    
    private <R extends AbstractEntity<?>> Object determinePropValue(final R owningEntity, final String propName, final EntityContainer<? extends AbstractEntity<?>> entityContainer) {
        if (entityContainer.isProxy()) {
            switch (proxyMode) {
            case STRICT:
                return instantiateStrictProxy(entityContainer.getResultType(), entityContainer.getId(), cache);
            case LAZY:
                return instantiateLazyProxy(entityContainer.getResultType(), owningEntity, entityContainer.getId(), propName);
            default:
                throw new IllegalStateException("Unknown proxy mode [" + proxyMode + "]");
            }
        } else if (entityContainer.isStrictProxy()) {
            return instantiateStrictProxy(entityContainer.getResultType(), entityContainer.getId(), cache);
        } else if (entityContainer.isEmpty()) {
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