package ua.com.fielden.platform.entity.query;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

public final class EntityContainer<R extends AbstractEntity<?>> {
    private final static String ID_PROPERTY_NAME = "id";

    private final Class<R> resultType;
    private R entity;
    private boolean shouldBeFetched;
    private final Map<String, Object> primitives = new HashMap<String, Object>();
    private final Map<String, ValueContainer> composites = new HashMap<String, ValueContainer>();
    private final Map<String, EntityContainer<? extends AbstractEntity<?>>> entities = new HashMap<String, EntityContainer<? extends AbstractEntity<?>>>();
    private final Map<String, Collection<EntityContainer<? extends AbstractEntity<?>>>> collections = new HashMap<String, Collection<EntityContainer<? extends AbstractEntity<?>>>>();

    public EntityContainer(final Class<R> resultType, final boolean shouldBeFetched) {
	this.resultType = resultType;
	this.shouldBeFetched = shouldBeFetched;
    }

    private int countAllDataItems () {
	return primitives.size() + entities.size() + composites.size() + collections.size();
    }

    public boolean isEmpty() {
	 return countAllDataItems()== 1 && primitives.containsKey(ID_PROPERTY_NAME) && getId() == null;
    }

    public boolean notYetInitialised() {
	return countAllDataItems() == 1 && getId() != null;
    }

    public boolean isInstantiated() {
	return entity != null;
    }

    public Long getId() {
	final Object idObject = primitives.get(ID_PROPERTY_NAME);
	return idObject != null ? ((Number) idObject).longValue() : null;//id;
    }

    public R instantiate(final EntityFactory entFactory, final boolean userViewOnly) {
	entity = userViewOnly ? entFactory.newPlainEntity(resultType, getId()) : entFactory.newEntity(resultType, getId());
	entity.setInitialising(true);
	for (final Map.Entry<String, Object> primPropEntry : primitives.entrySet()) {
	    try {
		setPropertyValue(entity, primPropEntry.getKey(), primPropEntry.getValue(), userViewOnly);
		//entity.set(primPropEntry.getKey(), primPropEntry.getValue());
	    } catch (final Exception e) {
		throw new IllegalStateException("Can't set value [" + primPropEntry.getValue() + "] of type ["
			+ (primPropEntry.getValue() != null ? primPropEntry.getValue().getClass().getName() : " unknown") + "] for property [" + primPropEntry.getKey()
			+ "] due to:" + e);
	    }
	}

	for (final Map.Entry<String, ValueContainer> compositePropEntry : composites.entrySet()) {
	    try {
		setPropertyValue(entity, compositePropEntry.getKey(), compositePropEntry.getValue().instantiate(), userViewOnly);
	    } catch (final Exception e) {
		throw new IllegalStateException("Can't set value [" + compositePropEntry.getValue() + "] of type [" + compositePropEntry.getValue().hibType + "] for property ["
	    + compositePropEntry.getKey() + "] due to:" + e);
	    }
	}
	for (final Map.Entry<String, EntityContainer<? extends AbstractEntity<?>>> entityEntry : entities.entrySet()) {
	    if (entityEntry.getValue() == null || entityEntry.getValue().notYetInitialised() || !entityEntry.getValue().shouldBeFetched) {
		setPropertyValue(entity, entityEntry.getKey(), null, userViewOnly);
	    } else if (entityEntry.getValue().isInstantiated()) {
		setPropertyValue(entity, entityEntry.getKey(), entityEntry.getValue().entity, userViewOnly);
	    } else {
		setPropertyValue(entity, entityEntry.getKey(), entityEntry.getValue().instantiate(entFactory, userViewOnly), userViewOnly);
	    }
	}

	for (final Map.Entry<String, Collection<EntityContainer<? extends AbstractEntity<?>>>> entityEntry : collections.entrySet()) {
	    Collection<AbstractEntity<?>> collectionalProp = null;
	    try {
		collectionalProp = entityEntry.getValue().getClass().newInstance(); // instantiating collection (Set or List)
	    } catch (final Exception e) {
		throw new RuntimeException("COULD NOT EXECUTE [collectionalProp = entityEntry.getValue().getClass().newInstance();] due to: " + e);
	    }
	    for (final EntityContainer<? extends AbstractEntity<?>> container : entityEntry.getValue()) {
		if (!container.notYetInitialised()) {
		    collectionalProp.add(container.instantiate(entFactory, userViewOnly));
		}
	    }
	    setPropertyValue(entity, entityEntry.getKey(), collectionalProp, userViewOnly);
	}

	if (!userViewOnly) {
	    EntityUtils.handleMetaProperties(entity);
	}

	entity.setInitialising(false);

	return entity;
    }

    private void setPropertyValue(final R entity, final String propName, final Object propValue, final boolean userViewOnly) {
	if (!userViewOnly || EntityAggregates.class.isAssignableFrom(resultType)) {
	    entity.set(propName, propValue);
	} else {
	    try {
		final Field field = Finder.findFieldByName(resultType, propName);
		field.setAccessible(true);
		field.set(entity, propValue);
		field.setAccessible(false);
	    } catch (final Exception e) {
		throw new RuntimeException("Can't set value for property " + propName + " due to:" + e.getMessage());
	    }
	}
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

    public Map<String, Collection<EntityContainer<? extends AbstractEntity<?>>>> getCollections() {
        return collections;
    }

    public void setShouldBeFetched(final boolean shouldBeFetched) {
        this.shouldBeFetched = shouldBeFetched;
    }
}