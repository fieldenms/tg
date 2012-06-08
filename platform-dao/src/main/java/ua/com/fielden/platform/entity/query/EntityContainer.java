package ua.com.fielden.platform.entity.query;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

public final class EntityContainer<R extends AbstractEntity<?>> {

    private final Class<R> resultType;
    private R entity;
    private final Map<String, Object> primitives = new HashMap<String, Object>();
    private final Map<String, ValueContainer> composites = new HashMap<String, ValueContainer>();
    private final Map<String, EntityContainer<? extends AbstractEntity<?>>> entities = new HashMap<String, EntityContainer<? extends AbstractEntity<?>>>();
    private final Map<String, CollectionContainer<? extends AbstractEntity<?>>> collections = new HashMap<String, CollectionContainer<? extends AbstractEntity<?>>>();

    public EntityContainer(final Class<R> resultType) {
	this.resultType = resultType;
    }

    private int countAllDataItems () {
	return primitives.size() + entities.size() + composites.size() + collections.size();
    }

    public boolean isEmpty() {
	 return countAllDataItems()== 1 && primitives.containsKey(AbstractEntity.ID) && getId() == null;
    }

    public boolean notYetInitialised() {
	return countAllDataItems() == 1 && getId() != null;
    }

    public boolean isInstantiated() {
	return entity != null;
    }

    public Long getId() {
	final Object idObject = primitives.get(AbstractEntity.ID);
	return idObject != null ? ((Number) idObject).longValue() : null;
    }

    public R instantiate(final EntityFactory entFactory, final boolean userViewOnly) {
	entity = userViewOnly ? entFactory.newPlainEntity(resultType, getId()) : entFactory.newEntity(resultType, getId());
	entity.setInitialising(true);
	final boolean unionEntity = AbstractUnionEntity.class.isAssignableFrom(resultType);

	for (final Map.Entry<String, Object> primPropEntry : primitives.entrySet()) {
	    setPropertyValue(entity, primPropEntry.getKey(), primPropEntry.getValue());
	}

	for (final Map.Entry<String, ValueContainer> compositePropEntry : composites.entrySet()) {
	    setPropertyValue(entity, compositePropEntry.getKey(), compositePropEntry.getValue().instantiate());
	}

	for (final Map.Entry<String, EntityContainer<? extends AbstractEntity<?>>> entityEntry : entities.entrySet()) {
	    final Object propValue = determinePropValue(entityEntry.getValue(), entFactory, userViewOnly);
	    setPropertyValue(entity, entityEntry.getKey(), propValue);
	    if (unionEntity && propValue != null) {
		((AbstractUnionEntity) entity).ensureUnion(entityEntry.getKey(), (AbstractEntity) propValue);
	    }
	}

	for (final Map.Entry<String, CollectionContainer<? extends AbstractEntity<?>>> entityEntry : collections.entrySet()) {
	    setPropertyValue(entity, entityEntry.getKey(), entityEntry.getValue().instantiate(entFactory, userViewOnly));
	}

	if (!userViewOnly) {
	    EntityUtils.handleMetaProperties(entity);
	}

	entity.setInitialising(false);

	return entity;
    }

    private Object determinePropValue(final EntityContainer<? extends AbstractEntity<?>> entityContainer, final EntityFactory entFactory, final boolean userViewOnly) {
	if (entityContainer == null || entityContainer.notYetInitialised()) {
	    return null;
	} else if (entityContainer.isInstantiated()) {
	    return entityContainer.entity;
	} else {
	    return entityContainer.instantiate(entFactory, userViewOnly);
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
	    throw new IllegalStateException("Can't set value [" + propValue + "] of type [" + (propValue != null ? propValue.getClass() : "?") + "] for property [" + propName + "] due to:" + e);
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