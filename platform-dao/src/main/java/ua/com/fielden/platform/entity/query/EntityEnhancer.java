package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.generation.DbVersion;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.utils.EntityUtils;
import static ua.com.fielden.platform.entity.query.fluent.query.from;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class EntityEnhancer<E extends AbstractEntity<?>> {
    private final static String ID_PROPERTY_NAME = "id";
    private Session session;
    private EntityFactory entityFactory;
    private Logger logger = Logger.getLogger(this.getClass());
    private MappingsGenerator mappingsGenerator;
    private DbVersion dbVersion;
    private final IFilter filter;
    private final String username;

    protected EntityEnhancer(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator, final DbVersion dbVersion, final IFilter filter, final String username) {
	this.session = session;
	this.entityFactory = entityFactory;
	this.mappingsGenerator = mappingsGenerator;
	this.dbVersion = dbVersion;
	this.filter = filter;
	this.username = username;
    }

    /**
     * Enhances entities according to provided fetch model.
     *
     * @param entities
     *            - entities that will enhanced
     * @param fetchModel
     * @param entitiesType
     * @return
     * @throws Exception
     */
    protected List<EntityContainer<E>> enhance(final List<EntityContainer<E>> entities, final fetch<E> fetchModel, final Class<E> entitiesType) throws Exception {
	if (fetchModel != null) {
	    final Map<String, fetch<? extends AbstractEntity<?>>> propertiesFetchModels = fetchModel.getFetchModels();

	    for (final Map.Entry<String, fetch<?>> entry : propertiesFetchModels.entrySet()) {
		final String propName = entry.getKey();
		final fetch<? extends AbstractEntity<?>> propFetchModel = entry.getValue();

		if (!EntityUtils.isPersistedEntityType(entitiesType) || //
			mappingsGenerator.getPropPersistenceInfoExplicitly(entitiesType, propName).isEntity() || //
			mappingsGenerator.getPropPersistenceInfoExplicitly(entitiesType, propName).isOne2OneId() //
		) {
		    logger.debug("enhancing property [" + propName + "]");
		    enhanceProperty(entities, propName, propFetchModel);
		}
	    }
	}

	return entities;
    }

    /**
     * Iterates through provided entities and collects information on the requested property - property value and list of all entities with such property value.
     *
     * @param entities
     * @param propertyName
     * @return
     */
    private Map<Long, List<EntityContainer<E>>> getEntityPropertyIds(final List<EntityContainer<E>> entities, final String propertyName) {
	final Map<Long, List<EntityContainer<E>>> propValuesMap = new HashMap<Long, List<EntityContainer<E>>>();
	logger.info("getting ids for property: " + propertyName);
	for (final EntityContainer<E> entity : entities) {
	    final EntityContainer<? extends AbstractEntity<?>> propEntity = entity.getEntities().get(propertyName);
	    if (propEntity != null && propEntity.getId() != null) {
		if (!propValuesMap.containsKey(propEntity.getId())) {
		    propValuesMap.put(propEntity.getId(), new ArrayList<EntityContainer<E>>());
		}
		propValuesMap.get(propEntity.getId()).add(entity);
	    }
	}
	return propValuesMap;
    }

    /**
     * Retrieves from the provided entity aggregates instances list of values for the specified property (of the specified type -- to cover polymorphic properties).
     *
     * @param entities
     * @param propName
     * @param propType
     * @return
     */
    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getRetrievedPropertyInstances(final List<EntityContainer<E>> entities, final String propName) {
	final List<EntityContainer<T>> propValues = new ArrayList<EntityContainer<T>>();
	for (final EntityContainer<E> entity : entities) {
	    final EntityContainer<T> prop = (EntityContainer<T>) entity.getEntities().get(propName);
	    if (prop != null && !prop.notYetInitialised()) {
		propValues.add(prop);
		prop.setShouldBeFetched(true);
	    }
	}
	return propValues;
    }

    /**
     * Performs nested initialisation of the properties of the provided entities according to provided fetch model(s).
     *
     * @param entities
     * @param fetchModels
     * @return
     * @throws Exception
     */
    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceProperty(final List<EntityContainer<E>> entities, final String propertyName, final fetch<T> fetchModel) throws Exception {
	// Obtaining map between property id and list of entities where this property occurs
	final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);
	logger.info("got ids count: " + propertyValuesIds.size());

	if (propertyValuesIds.size() > 0) {
	    // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
	    final List<EntityContainer<T>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
	    logger.info("got retrievedPropertyInstances count: " + retrievedPropertyInstances.size());
	    final List<EntityContainer<T>> enhancedPropInstances;
	    if (retrievedPropertyInstances.size() == 0) {
		enhancedPropInstances = getDataInBatches(new ArrayList<Long>(propertyValuesIds.keySet()), fetchModel);
	    } else {
		enhancedPropInstances = new EntityEnhancer<T>(session, entityFactory, mappingsGenerator, dbVersion, filter, username).enhance(retrievedPropertyInstances, fetchModel, fetchModel.getEntityType());
	    }

	    // Replacing in entities the proxies of properties with properly enhanced property instances.
	    for (final EntityContainer<? extends AbstractEntity<?>> enhancedPropInstance : enhancedPropInstances) {
		final List<EntityContainer<E>> thisPropertyEntities = propertyValuesIds.get(enhancedPropInstance.getId());
		for (final EntityContainer<E> thisPropertyEntity : thisPropertyEntities) {
		    thisPropertyEntity.getEntities().put(propertyName, enhancedPropInstance);
		}
	    }
	}

	return entities;
    }

    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getDataInBatches(final List<Long> ids, final fetch<T> fetchModel) throws Exception {
	final List<EntityContainer<T>> result = new ArrayList<EntityContainer<T>>();
	final Long[] allParentIds = new ArrayList<Long>(ids).toArray(new Long[] {});

	final Integer batchSize = 990;
	Integer from = 0;
	Integer to = batchSize;
	boolean endReached = false;
	while (!endReached) {
	    if (to >= allParentIds.length) {
		endReached = true;
		to = allParentIds.length;
	    }
	    final Long[] batch = Arrays.copyOfRange(allParentIds, from, to);
	    @SuppressWarnings("unchecked")
	    final EntityResultQueryModel<T> currTypePropertyModel = select(fetchModel.getEntityType()).where().prop(ID_PROPERTY_NAME).in().values(batch).model()/*.getModelWithAbstractEntities()*/;
	    @SuppressWarnings("unchecked")
	    final List<EntityContainer<T>> properties = new EntityFetcher<T>(session, entityFactory, mappingsGenerator, dbVersion, filter, username).listContainers(from(currTypePropertyModel).with(fetchModel).build(), null, null);
	    result.addAll(properties);
	    from = to;
	    to = to + batchSize;
	}

	return result;
    }
}