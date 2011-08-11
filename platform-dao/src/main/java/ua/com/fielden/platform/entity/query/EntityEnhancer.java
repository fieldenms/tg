package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class EntityEnhancer<E extends AbstractEntity> {
    private final static String ID_PROPERTY_NAME = "id";
    private Session session;
    private EntityFactory entityFactory;
    private Logger logger = Logger.getLogger(this.getClass());
    private MappingsGenerator mappingsGenerator;

    protected EntityEnhancer() {
    }

    protected EntityEnhancer(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator) {
	this.session = session;
	this.entityFactory = entityFactory;
	this.mappingsGenerator = mappingsGenerator;
    }

    protected fetch<E> enhanceFetchModelWithKeyProperties(final fetch<E> fetchModel, final Class<E> entitiesType) {
	final fetch<E> enhancedFetchModel = fetchModel != null ? fetchModel : new fetch(entitiesType);
	final List<String> keyMemberNames = Finder.getFieldNames(Finder.getKeyMembers(entitiesType));
	for (final String keyProperty : keyMemberNames) {
	    final Class propType = PropertyTypeDeterminator.determinePropertyType(entitiesType, keyProperty);
	    if (AbstractEntity.class.isAssignableFrom(propType) && !enhancedFetchModel.getFetchModels().containsKey(keyProperty)) {
		enhancedFetchModel.with(keyProperty, new fetch(propType));
	    }
	}
	return enhancedFetchModel;
    }

    /**
     * Enhances entities according to provided fetch model.
     *
     * @param entities - entities that will enhanced
     * @param fetchModel
     * @param entitiesType
     * @return
     */
    protected List<EntityContainer<E>> enhance(final List<EntityContainer<E>> entities, final fetch<E> fetchModel, final Class<E> entitiesType) {
//	if (fetchModel != null) {
//	    final Map<String, fetch> propertiesFetchModels = fetchModel.getFetchModels();
//
//	    for (final Map.Entry<String, fetch> entry : propertiesFetchModels.entrySet()) {
//		final String propName = entry.getKey();
//		final fetch propFetchModel = entry.getValue();
//
//		if (mappingExtractor.isNotPersisted(entitiesType) || //
//			mappingExtractor.isManyToOne(entitiesType, propName) || //
//			mappingExtractor.isAny(entitiesType, propName) || //
//			mappingExtractor.isOneToOneDetails(entitiesType, propName) //
//		) {
//		    logger.debug("enhancing property [" + propName + "]");
//		    enhanceProperty(entities, propName, propFetchModel);
//		} else if (mappingExtractor.isSimpleValue(entitiesType, propName)) {
//		    logger.debug("enhancing property [" + propName + "]: no enhancing is required because it is SimpleValue.");
//		    // e.g. properties that getting here usually has hibernate custom user type and are not classic "entities", and doesn't require enhancing
//		} else if (mappingExtractor.isCollection(entitiesType, propName)) {
//		    final String parentPropName = mappingExtractor.getParentPropertyName(entitiesType, propName);
//		    final Class propertyType = PropertyTypeDeterminator.determineClass(entitiesType, propName, true, false);
//		    String indexPropName = null;
//		    if (mappingExtractor.isList(entitiesType, propName)) {
//			indexPropName = mappingExtractor.getIndexPropertyName(entitiesType, propName);
//		    }
//		    logger.debug("enhancing collectional property [" + propName + "]");
//		    enhanceCollectional(entities, propName, propertyType, parentPropName, indexPropName, propFetchModel);
//		} else {
//		    throw new IllegalArgumentException("Unsupported mapping type: === parent entity type is " + entitiesType + "; property name is " + propName);
//		}
//	    }
//	}

	return entities;
    }

    /**
     * Iterates through provided entities and collects information on the requested property - property value and list of all entities with such property
     * value.
     *
     * @param entities
     * @param propertyName
     * @return
     */
    private Map<Long, List<EntityContainer<E>>> getEntityPropertyIds(final List<EntityContainer<E>> entities, final String propertyName) {
	final Map<Long, List<EntityContainer<E>>> propValuesMap = new HashMap<Long, List<EntityContainer<E>>>();
	logger.info("getting ids for property: " + propertyName);
	for (final EntityContainer<E> entity : entities) {
	    final EntityContainer propEntity = entity.entities.get(propertyName);
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
    private List<EntityContainer<AbstractEntity>> getRetrievedPropertyInstances(final List<EntityContainer<E>> entities, final String propName) {
	final List<EntityContainer<AbstractEntity>> propValues = new ArrayList<EntityContainer<AbstractEntity>>();
	for (final EntityContainer<?> entity : entities) {
	    final EntityContainer prop = entity.entities.get(propName);
	    if (prop != null && !prop.notYetInitialised()) {
		propValues.add(prop);
		prop.shouldBeFetched = true;
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
    private List<EntityContainer<E>> enhanceProperty(final List<EntityContainer<E>> entities, final String propertyName, final fetch fetchModel) throws Exception {
	// Obtaining map between property id and list of entities where this property occurs
	final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);
	logger.info("got ids count: " + propertyValuesIds.size());

	if (propertyValuesIds.size() > 0) {
	    // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
	    final List<EntityContainer<AbstractEntity>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
	    logger.info("got retrievedPropertyInstances count: " + retrievedPropertyInstances.size());
	    final List<EntityContainer> enhancedPropInstances;
	    if (retrievedPropertyInstances.size() == 0) {
		enhancedPropInstances = getDataInBatches(new ArrayList<Long>(propertyValuesIds.keySet()), fetchModel);
	    } else {
		enhancedPropInstances = new EntityEnhancer(session, entityFactory, mappingsGenerator).enhance(retrievedPropertyInstances, fetchModel, fetchModel.getEntityType());
	    }

	    // Replacing in entities the proxies of properties with properly enhanced property instances.
	    for (final EntityContainer enhancedPropInstance : enhancedPropInstances) {
		final List<EntityContainer<E>> thisPropertyEntities = propertyValuesIds.get(enhancedPropInstance.getId());
		for (final EntityContainer<E> thisPropertyEntity : thisPropertyEntities) {
		    thisPropertyEntity.entities.put(propertyName, enhancedPropInstance);
		}
	    }
	}

	return entities;
    }

    private List<EntityContainer> getDataInBatches(final List<Long> ids, final fetch<E> fetchModel) throws Exception {
	final List<EntityContainer> result = new ArrayList<EntityContainer>();
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
	    final QueryModel currTypePropertyModel = select(fetchModel.getEntityType()).where().prop(ID_PROPERTY_NAME).in().values(batch).model()/*.getModelWithAbstractEntities()*/;
	    @SuppressWarnings("unchecked")
	    final List<EntityContainer> properties = new EntityFetcher(session, entityFactory, mappingsGenerator).listContainers(currTypePropertyModel, null, null, fetchModel);
	    result.addAll(properties);
	    from = to;
	    to = to + batchSize;
	}

	return result;
    }

    /**
     * Performs nested initialisation of the collectional properties of the provided entities according to provided fetch model. It should be noted, that due to implementation
     * specifics the instances returned are not longer associated with current hibernate session. In case of necessity they should re-associated with session via merge method.
     *
     * @param entitiesToBeEnhanced
     * @param propertyName
     * @param parentPropName
     * @param indexPropName
     * @param propertyFetchModel
     * @return
     * @throws Exception
     */
    private List<EntityContainer<E>> enhanceCollectional(final List<EntityContainer<E>> entitiesToBeEnhanced, final String propertyName, final Class propType, final String parentPropName, final String indexPropName, final fetch<E> fetchModel) throws Exception {
	// collect parental ids
	final Map<Long, EntityContainer<E>> parentIds = new HashMap<Long, EntityContainer<E>>();
	for (final EntityContainer<E> parentEntity : entitiesToBeEnhanced) {
	    parentIds.put(parentEntity.getId(), parentEntity);
	}
	final List<EntityContainer> properties = getCollectionalDataInBatches(parentIds.keySet(), parentPropName, indexPropName, fetchModel);

	// group retrieved collections by parents
	@SuppressWarnings("unchecked")
	final Map<Long, List<EntityContainer>> results = new HashMap<Long, List<EntityContainer>>();
	for (final EntityContainer<?> collectionalItem : properties) {
	    final Long currentParentId = collectionalItem.entities.get(parentPropName).getId();
	    if (!results.containsKey(currentParentId)) {
		results.put(currentParentId, new ArrayList<EntityContainer>());
	    }
	    // assign collectional item parent property reference to its already fetched parent
	    collectionalItem.entities.put(parentPropName, parentIds.get(currentParentId));
	    results.get(currentParentId).add(collectionalItem);
	}

	// set the retrieved data for those entities where collectional property is not empty
	for (final Map.Entry<Long, List<EntityContainer>> resultEntry : results.entrySet()) {
	    // assigns initialised collection to parent collectional property (lazy-collection is already evicted)
	    final EntityContainer<E> entity = parentIds.get(resultEntry.getKey());
	    if (List.class.isAssignableFrom(propType)) {
		entity.collections.put(propertyName, resultEntry.getValue());
	    } else if (Set.class.isAssignableFrom(propType)) {
		// TODO need to implement proper collection instantiation (prop type)
		entity.collections.put(propertyName, new HashSet<EntityContainer>(resultEntry.getValue()));
	    } else {
		throw new UnsupportedOperationException("Fetching via models for collections of type ["
			+ /* parentIds.get(resultEntry.getKey()).get(propertyName).getClass() */" xxx " + "] is not yet supported.");
	    }
	}

	return entitiesToBeEnhanced;
    }

    private List<EntityContainer> getCollectionalDataInBatches(final Set<Long> parentIds, final String parentPropName, final String indexPropName, final fetch<E> fetchModel) throws Exception {
	final List<EntityContainer> result = new ArrayList<EntityContainer>();
	final String idProp = parentPropName  + "." + ID_PROPERTY_NAME;
	final Long[] allParentIds = new ArrayList<Long>(parentIds).toArray(new Long[]{});

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
		final EntityQueryProgressiveInterfaces.ICompleted base = select(fetchModel.getEntityType()).where().prop(idProp).in().values(batch);
		final QueryModel currTypePropertyModel = (indexPropName != null ? base.orderBy().prop(parentPropName).asc().orderBy().prop(indexPropName).asc() : base.orderBy().prop(parentPropName).asc()).modelAsEntity(fetchModel.getEntityType())/*.getModelWithAbstractEntities()*/;
		@SuppressWarnings("unchecked")
		// final List<EntityContainer> properties = new Fetcher().listContainersWithoutKeyEnhanced(currTypePropertyModel, null, null);
		final List<EntityContainer> properties = new EntityFetcher(session, entityFactory, mappingsGenerator).listContainers(currTypePropertyModel, null, null, fetchModel);
		result.addAll(properties);
		// TODO need to optimise -- WagonClass in WagonClassCompatibility is re-retrieved, while already available
		from = to;
		to =  to + batchSize;
	}

	return result;
    }

}
