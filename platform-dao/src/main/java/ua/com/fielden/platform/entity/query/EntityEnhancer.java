package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class EntityEnhancer<E extends AbstractEntity<?>> {
    private final static String ID_PROPERTY_NAME = "id";
    private final EntityFetcher fetcher;

    protected EntityEnhancer(final EntityFetcher fetcher) {
	this.fetcher = fetcher;
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
    protected List<EntityContainer<E>> enhance(final List<EntityContainer<E>> entities, final fetch<E> fetchModel) throws Exception {
	if (fetchModel != null) {
	    final Map<String, fetch<? extends AbstractEntity<?>>> propertiesFetchModels = fetchModel.getFetchModels();

	    for (final Map.Entry<String, fetch<?>> entry : propertiesFetchModels.entrySet()) {
		final String propName = entry.getKey();
		final fetch<? extends AbstractEntity<?>> propFetchModel = entry.getValue();
		final PropertyPersistenceInfo ppi = fetcher.getMappingsGenerator().getPropPersistenceInfoExplicitly(fetchModel.getEntityType(), propName);

		if (/*!EntityUtils.isPersistedEntityType(entitiesType) || //*/ppi.isEntity() || ppi.isOne2OneId()) {
		    enhanceProperty(entities, propName, propFetchModel);
		} else if (ppi.isCollection()) {
		    //enhanceCollectional(entities, propName, HashSet.class, "user", null, propFetchModel);
		}
	    }
	}

	return entities;
    }

    private Map<Long, List<EntityContainer<E>>> getEntityPropertyIds(final List<EntityContainer<E>> entities, final String propertyName) {
	final Map<Long, List<EntityContainer<E>>> propValuesMap = new HashMap<Long, List<EntityContainer<E>>>();
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

    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceProperty(final List<EntityContainer<E>> entities, final String propertyName, final fetch<T> fetchModel) throws Exception {
	// Obtaining map between property id and list of entities where this property occurs
	final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);

	if (propertyValuesIds.size() > 0) {
	    // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
	    final List<EntityContainer<T>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
	    final List<EntityContainer<T>> enhancedPropInstances = (retrievedPropertyInstances.size() == 0) ? //
		getDataInBatches(new ArrayList<Long>(propertyValuesIds.keySet()), fetchModel) : //
	    	new EntityEnhancer<T>(fetcher).enhance(retrievedPropertyInstances, fetchModel);

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
	    final EntityResultQueryModel<T> currTypePropertyModel = select(fetchModel.getEntityType()).where().prop(ID_PROPERTY_NAME).in().values(batch).model();
	    @SuppressWarnings("unchecked")
	    final List<EntityContainer<T>> properties = fetcher.listContainers(from(currTypePropertyModel).with(fetchModel).build(), null, null);
	    result.addAll(properties);
	    from = to;
	    to = to + batchSize;
	}

	return result;
    }

//    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceCollectional(final List<EntityContainer<E>> entitiesToBeEnhanced, final String propertyName, final Class propType, final String parentPropName, final String indexPropName, final fetch<T> fetchModel) throws Exception {
//        // collect parental ids
//        final Map<Long, EntityContainer<E>> parentIds = new HashMap<Long, EntityContainer<E>>();
//        for (final EntityContainer<E> parentEntity : entitiesToBeEnhanced) {
//            parentIds.put(parentEntity.getId(), parentEntity);
//        }
//        final List<EntityContainer<T>> properties = getCollectionalDataInBatches(parentIds.keySet(), parentPropName, indexPropName, fetchModel);
//
//        // group retrieved collections by parents
//        @SuppressWarnings("unchecked")
//        final Map<Long, List<EntityContainer<T>>> results = new HashMap<Long, List<EntityContainer<T>>>();
//        for (final EntityContainer<T> collectionalItem : properties) {
//            final Long currentParentId = collectionalItem.getEntities().get(parentPropName).getId();
//            if (!results.containsKey(currentParentId)) {
//                results.put(currentParentId, new ArrayList<EntityContainer<T>>());
//            }
//            // assign collectional item parent property reference to its already fetched parent
//            collectionalItem.getEntities().put(parentPropName, parentIds.get(currentParentId));
//            results.get(currentParentId).add(collectionalItem);
//        }
//
//        // set the retrieved data for those entities where collectional property is not empty
//        for (final Map.Entry<Long, List<EntityContainer<T>>> resultEntry : results.entrySet()) {
//            // assigns initialised collection to parent collectional property (lazy-collection is already evicted)
//            final EntityContainer<E> entity = parentIds.get(resultEntry.getKey());
//            if (List.class.isAssignableFrom(propType)) {
//        	final List<EntityContainer<? extends AbstractEntity<?>>> value = new ArrayList<EntityContainer<? extends AbstractEntity<?>>>(resultEntry.getValue());
//                entity.getCollections().put(propertyName, value);
//            } else if (Set.class.isAssignableFrom(propType)) {
//                // TODO need to implement proper collection instantiation (prop type)
//                entity.getCollections().put(propertyName, new HashSet<EntityContainer<? extends AbstractEntity<?>>>(resultEntry.getValue()));
//            } else {
//                throw new UnsupportedOperationException("Fetching via models for collections of type ["
//                        + /* parentIds.get(resultEntry.getKey()).get(propertyName).getClass() */propType + "] is not yet supported.");
//            }
//        }
//
//        return entitiesToBeEnhanced;
//    }
//
//    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getCollectionalDataInBatches(final Set<Long> parentIds, final String parentPropName, final String indexPropName, final fetch<T> fetchModel) throws Exception {
//        final List<EntityContainer<T>> result = new ArrayList<EntityContainer<T>>();
//        final String idProp = parentPropName  + "." + ID_PROPERTY_NAME;
//        final Long[] allParentIds = new ArrayList<Long>(parentIds).toArray(new Long[]{});
//
//        final Integer batchSize = 990;
//        Integer from = 0;
//        Integer to = batchSize;
//        boolean endReached = false;
//        while (!endReached) {
//            if (to >= allParentIds.length) {
//                endReached = true;
//                to = allParentIds.length;
//            }
//            final Long[] batch = Arrays.copyOfRange(allParentIds, from, to);
//            System.out.println("fetchModel.getEntityType() = " + fetchModel.getEntityType() + " idProp = " + idProp + " batch = " + Arrays.asList(batch));
//                @SuppressWarnings("unchecked")
//
//                final EntityResultQueryModel<T> currTypePropertyModel = select(fetchModel.getEntityType()).where().prop(idProp).in().values(batch).model();
//                final OrderingModel currTypePropertyOrderModel = indexPropName != null ? orderBy().prop(parentPropName).asc().prop(indexPropName).asc().model() : orderBy().prop(parentPropName).asc().model();
//                @SuppressWarnings("unchecked")
//                // final List<EntityContainer> properties = new Fetcher().listContainersWithoutKeyEnhanced(currTypePropertyModel, null, null);
//                final List<EntityContainer<T>> properties = new EntityFetcher<T>(session, entityFactory, mappingsGenerator, dbVersion, filter, username).listContainers(from(currTypePropertyModel).with(currTypePropertyOrderModel).with(fetchModel).build(), null, null);
//                result.addAll(properties);
//                // TODO need to optimise -- WagonClass in WagonClassCompatibility is re-retrieved, while already available
//                from = to;
//                to =  to + batchSize;
//        }
//
//        return result;
//    }
}