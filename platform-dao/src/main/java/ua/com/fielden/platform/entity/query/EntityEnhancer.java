package ua.com.fielden.platform.entity.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class EntityEnhancer<E extends AbstractEntity<?>> {
    private final EntityFetcher fetcher;
    private final DomainMetadataAnalyser domainMetadataAnalyser;

    protected EntityEnhancer(final EntityFetcher fetcher, final DomainMetadataAnalyser domainMetadataAnalyser) {
	this.fetcher = fetcher;
	this.domainMetadataAnalyser = domainMetadataAnalyser;
    }

    protected List<EntityContainer<E>> enhance(final List<EntityContainer<E>> entities, final FetchModel<E> fetchModel) throws Exception {
	if (fetchModel != null) {
	    final Map<String, fetch<? extends AbstractEntity<?>>> propertiesFetchModels = fetchModel.getFetchModels();

	    for (final Map.Entry<String, fetch<?>> entry : propertiesFetchModels.entrySet()) {
		final String propName = entry.getKey();
		final fetch<? extends AbstractEntity<?>> propFetchModel = entry.getValue();
		if (fetchModel.getEntityType() != EntityAggregates.class) {
		    final PropertyMetadata ppi = domainMetadataAnalyser.getPropPersistenceInfoExplicitly(fetchModel.getEntityType(), propName);
		    if (ppi == null || ppi.isCollection()) {
			// TODO replace with EntityTree metadata (wrt collectional properties retrieval)
			final List<Field> collProps = EntityUtils.getCollectionalProperties(fetchModel.getEntityType());
			for (final Field field : collProps) {
			    if (field.getName().equals(propName)) {
				//final String linkPropName = field.getAnnotation(IsProperty.class).linkProperty();
				final String linkPropName = Finder.findLinkProperty(fetchModel.getEntityType(), propName);
				enhanceCollectional(entities, propName, field.getType(), linkPropName, propFetchModel);
			    }
			}
		    } else if (ppi.isEntityOfPersistedType() || ppi.isOne2OneId() || ppi.isUnionEntity()) {
			enhanceProperty(entities, propName, propFetchModel);
		    } else {
			// do notshing
		    }
		} else {
		    enhanceProperty(entities, propName, propFetchModel);
		}

	    }
	}

	return entities;
    }

    private Map<Long, List<EntityContainer<E>>> getEntityPropertyIds(final List<EntityContainer<E>> entities, final String propertyName) {
	final Map<Long, List<EntityContainer<E>>> propValuesMap = new HashMap<Long, List<EntityContainer<E>>>();
	for (final EntityContainer<E> entity : entities) {
	    if (entity == null) {
		throw new IllegalStateException("Entity is null!");
	    }
	    if (entity.getEntities() == null) {
		throw new IllegalStateException("Entity.getEntities() is null!");
	    }
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

    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getRetrievedPropertyInstances(final List<EntityContainer<E>> entities, final String propName) {
	final List<EntityContainer<T>> propValues = new ArrayList<EntityContainer<T>>();
	for (final EntityContainer<E> entity : entities) {
	    final EntityContainer<T> prop = (EntityContainer<T>) entity.getEntities().get(propName);
	    if (prop != null && !prop.notYetInitialised()) {
		propValues.add(prop);
	    }
	}
	return propValues;
    }

    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceProperty(final List<EntityContainer<E>> entities, final String propertyName, final fetch<T> fetchModel)
	    throws Exception {
	// Obtaining map between property id and list of entities where this property occurs
	final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);

	if (propertyValuesIds.size() > 0) {
	    // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
	    final List<EntityContainer<T>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
	    // IMPORTANT: it is assumed that EntityContainer can contain either only id or all props at once. Such assumption relied on fact that once join to property has been made all its columns had been yielded automatically.
	    final List<EntityContainer<T>> enhancedPropInstances = (retrievedPropertyInstances.size() == 0) ? //
	    getDataInBatches(new ArrayList<Long>(propertyValuesIds.keySet()), fetchModel)
		    : //
		    new EntityEnhancer<T>(fetcher, domainMetadataAnalyser).enhance(retrievedPropertyInstances, new FetchModel(fetchModel, domainMetadataAnalyser));

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
	    final EntityResultQueryModel<T> currTypePropertyModel = select(fetchModel.getEntityType()).where().prop(AbstractEntity.ID).in().values(batch).model();
	    final List<EntityContainer<T>> properties = fetcher.listContainers(from(currTypePropertyModel).with(fetchModel).model(), null, null);
	    result.addAll(properties);
	    from = to;
	    to = to + batchSize;
	}

	return result;
    }

    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceCollectional(final List<EntityContainer<E>> entitiesToBeEnhanced, final String propertyName, final Class propType, final String parentPropName, final fetch<T> fetchModel)
	    throws Exception {
	// collect parental ids
	final Map<Long, EntityContainer<E>> parentIds = new HashMap<Long, EntityContainer<E>>();
	for (final EntityContainer<E> parentEntity : entitiesToBeEnhanced) {
	    parentIds.put(parentEntity.getId(), parentEntity);
	}
	final List<EntityContainer<T>> properties = getCollectionalDataInBatches(parentIds.keySet(), parentPropName, fetchModel);

	// group retrieved collections by parents
	final Map<Long, List<EntityContainer<T>>> results = new HashMap<Long, List<EntityContainer<T>>>();
	for (final EntityContainer<T> collectionalItem : properties) {
	    final Long currentParentId = collectionalItem.getEntities().get(parentPropName).getId();
	    if (!results.containsKey(currentParentId)) {
		results.put(currentParentId, new ArrayList<EntityContainer<T>>());
	    }
	    // assign collectional item parent property reference to its already fetched parent
	    collectionalItem.getEntities().put(parentPropName, parentIds.get(currentParentId));
	    results.get(currentParentId).add(collectionalItem);
	}

	// set the retrieved data for those entities where collectional property is not empty
	for (final Map.Entry<Long, List<EntityContainer<T>>> resultEntry : results.entrySet()) {
	    // assigns initialised collection to parent collectional property (lazy-collection is already evicted)
	    final EntityContainer<E> entity = parentIds.get(resultEntry.getKey());
	    if (SortedSet.class.equals(propType)) {
		entity.getCollections().put(propertyName, new CollectionContainer<T>(new TreeSet<T>(), resultEntry.getValue()));
	    } else if (Set.class.equals(propType)) {
		entity.getCollections().put(propertyName, new CollectionContainer<T>(new HashSet<T>(), resultEntry.getValue()));
	    } else {
		throw new UnsupportedOperationException("Fetching via models for collections of type [" + propType + "] is not yet supported.");
	    }
	}

	return entitiesToBeEnhanced;
    }

    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getCollectionalDataInBatches(final Set<Long> parentIds, final String parentPropName, final fetch<T> fetchModel)
	    throws Exception {
	final List<EntityContainer<T>> result = new ArrayList<EntityContainer<T>>();
	final String idProp = parentPropName;
	final Long[] allParentIds = new ArrayList<Long>(parentIds).toArray(new Long[] {});

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
	    final EntityResultQueryModel<T> currTypePropertyModel = select(fetchModel.getEntityType()).where().prop(idProp).in().values(batch).model();
	    final List<EntityContainer<T>> properties = fetcher.listContainers(from(currTypePropertyModel).with(fetchModel).model(), null, null);
	    result.addAll(properties);
	    // TODO need to optimise -- WagonClass in WagonClassCompatibility is re-retrieved, while already available
	    from = to;
	    to = to + batchSize;
	}

	return result;
    }
}