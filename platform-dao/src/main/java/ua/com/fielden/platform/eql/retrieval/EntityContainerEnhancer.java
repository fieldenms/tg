package ua.com.fielden.platform.eql.retrieval;

import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.query.*;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Iterables.partition;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliterator;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class EntityContainerEnhancer<E extends AbstractEntity<?>> {

    private static final Integer BATCH_SIZE = 990;

    private final EntityContainerFetcher fetcher;
    private final DomainMetadataAnalyser domainMetadataAnalyser;
    private final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;
    private static final Logger logger = getLogger(EntityContainerEnhancer.class);

    protected EntityContainerEnhancer(final EntityContainerFetcher fetcher, final DomainMetadataAnalyser domainMetadataAnalyser, final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        this.fetcher = fetcher;
        this.domainMetadataAnalyser = domainMetadataAnalyser;
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;
    }

    /**
     * Enhances a list of entity containers.
     * 
     * @param entities
     * @param fetchModel
     * @return
     * @throws Exception
     */
    protected List<EntityContainer<E>> enhance(final List<EntityContainer<E>> entities, final IRetrievalModel<E> fetchModel, final Map<String, Object> paramValues) {
        if (entities.isEmpty() || fetchModel == null) {
            return entities;
        }

        final Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> propertiesFetchModels = fetchModel.getRetrievalModels();

        for (final Map.Entry<String, EntityRetrievalModel<?>> entry : propertiesFetchModels.entrySet()) {
            final String propName = entry.getKey();
            final EntityRetrievalModel<? extends AbstractEntity<?>> propFetchModel = entry.getValue();

            if (propFetchModel.isFetchIdOnly()) {
                assignIdOnlyProxiedResultTypeToIdOnlyEntityProperty(entities, propName, propFetchModel.getEntityType());
            } else {
                if (fetchModel.getEntityType() != EntityAggregates.class) {
                    final PropertyMetadata ppi = domainMetadataAnalyser.getPropPersistenceInfoExplicitly(fetchModel.getEntityType(), propName);
                    //System.out.println("*** ENHANCING entity [" + fetchModel.getEntityType().getSimpleName() + "] property [" + propName +"] with fetch: " + propFetchModel + " ppi = " + ppi.getType());

                    if (ppi == null || ppi.isCollection()) {
                        // TODO replace with EntityTree metadata (wrt collectional properties retrieval)
                        final List<Field> collProps = EntityUtils.getCollectionalProperties(fetchModel.getEntityType());
                        for (final Field field : collProps) {
                            if (field.getName().equals(propName)) {
                                //final String linkPropName = field.getAnnotation(IsProperty.class).linkProperty();
                                final String linkPropName = Finder.findLinkProperty(fetchModel.getEntityType(), propName);
                                enhanceCollectional(entities, propName, field.getType(), linkPropName, propFetchModel, paramValues);
                            }
                        }
                    } else {
                        if (ppi.isUnionEntity()) {
                            enhanceProperty(entities, propName, propFetchModel, paramValues);
                        } else {
                            try {
                                final String linkPropName = Finder.findLinkProperty(fetchModel.getEntityType(), propName);
                                enhancePropertyWithLinkToParent(entities, propName, propFetchModel, linkPropName, paramValues);
                            } catch (final Exception e) {
                                if (ppi.isEntityOfPersistedType() || ppi.isOne2OneId()) {
                                    enhanceProperty(entities, propName, propFetchModel, paramValues);
                                } else {
                                    // logger.debug(format("Property [%s] of type [%s] can't be fetched with model: %s.", propName, fetchModel.getEntityType(), entry.getValue()));
                                }
                            }
                        }
                    }
                } else {
                    enhanceProperty(entities, propName, propFetchModel, paramValues);
                }
            }
        }

        assignProxiedResultTypeToContainers(entities, fetchModel);
        assignInstrumentationSetting(entities, fetchModel);

        return entities;
    }

    private void assignInstrumentationSetting(final List<EntityContainer<E>> entities, final IRetrievalModel<E> fetchModel) {
        if (fetchModel.isInstrumented()) {
            for (final EntityContainer<E> entityContainer : entities) {
                entityContainer.mkInstrumented();
            }
        }
    }

    private <T extends AbstractEntity<?>> Class<? extends T> determineProxiedResultTypeFromFetchModel(final IRetrievalModel<T> fetchModel) {
        final DateTime st = new DateTime();
        final Class<? extends T>  proxiedType = EntityProxyContainer.proxy(fetchModel.getEntityType(), fetchModel.getProxiedProps().toArray(new String[] {}));
        final Period pd = new Period(st, new DateTime());
        // logger.debug(format("Constructing proxy type [" + fetchModel.getEntityType().getSimpleName() + "] duration: %s m %s s %s ms.", pd.getMinutes(), pd.getSeconds(), pd.getMillis()));
        return proxiedType;
    }

    private void assignProxiedResultTypeToContainers(final List<EntityContainer<E>> entities, final IRetrievalModel<E> fetchModel) {
        if (fetchModel.getEntityType() != EntityAggregates.class) {
            final Class<? extends E> proxiedResultType = determineProxiedResultTypeFromFetchModel(fetchModel);

            for (final EntityContainer<E> entContainer : entities) {
                entContainer.setProxiedResultType(proxiedResultType);
            }
        }
    }

    private <T extends AbstractEntity<?>> void assignIdOnlyProxiedResultTypeToIdOnlyEntityProperty(final List<EntityContainer<E>> entities, final String propName, final Class<T> originalPropertyResultType) {
        if (originalPropertyResultType != EntityAggregates.class) {
            final Class<? extends T> proxiedResultType = idOnlyProxiedEntityTypeCache.getIdOnlyProxiedTypeFor(originalPropertyResultType);
            for (final EntityContainer<E> entContainer : entities) {
                final EntityContainer<T> propContainer = (EntityContainer<T>) entContainer.getEntities().get(propName);
                if (propContainer != null) {
                    propContainer.setProxiedResultType(proxiedResultType);
                }
            }
        }
    }

    private Map<Long, List<EntityContainer<E>>> getEntityPropertyIds(final List<EntityContainer<E>> entities, final String propertyName) {
        final Map<Long, List<EntityContainer<E>>> propValuesMap = new HashMap<>();
        for (final EntityContainer<E> entity : entities) {
            if (entity.isEmpty()) {
                throw new IllegalStateException("Entity is null!");
            }
            if (entity.getEntities() == null) {
                throw new IllegalStateException("Entity.getEntities() is null!");
            }
            final EntityContainer<? extends AbstractEntity<?>> propEntity = entity.getEntities().get(propertyName);
            if (propEntity != null && !propEntity.isEmpty() && propEntity.getId() != null) {
                if (!propValuesMap.containsKey(propEntity.getId())) {
                    propValuesMap.put(propEntity.getId(), new ArrayList<EntityContainer<E>>());
                }
                propValuesMap.get(propEntity.getId()).add(entity);
            }
        }
        return propValuesMap;
    }

    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getRetrievedPropertyInstances(final List<EntityContainer<E>> entities, final String propName) {
        final List<EntityContainer<T>> propValues = new ArrayList<>();
        for (final EntityContainer<E> entity : entities) {
            final EntityContainer<T> prop = (EntityContainer<T>) entity.getEntities().get(propName);
            if (prop != null && !prop.isEmpty() && !prop.notYetInitialised()) {
                propValues.add(prop);
            }
        }
        return propValues;
    }

    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhancePropertyWithLinkToParent(final List<EntityContainer<E>> entities, final String propertyName, final EntityRetrievalModel<T> fetchModel, final String linkPropName, final Map<String, Object> paramValues)
            throws Exception {
        // Obtaining map between property id and list of entities where this property occurs
        final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);

        if (propertyValuesIds.size() > 0) {
            // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
            final List<EntityContainer<T>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
            // IMPORTANT: it is assumed that EntityContainer can contain either only id or all props at once. Such assumption relied on fact that once join to property has been made all its columns had been yielded automatically.
            final List<EntityContainer<T>> enhancedPropInstances = retrievedPropertyInstances.isEmpty()
                    ? getDataInBatches(propertyValuesIds.keySet(), ID, fetchModel, paramValues)
                    :  new EntityContainerEnhancer<T>(fetcher, domainMetadataAnalyser, idOnlyProxiedEntityTypeCache)
                            .enhance(retrievedPropertyInstances, fetchModel, paramValues);

            // Replacing in entities the proxies of properties with properly enhanced property instances.
            for (final EntityContainer<? extends AbstractEntity<?>> enhancedPropInstance : enhancedPropInstances) {
                final List<EntityContainer<E>> thisPropertyEntities = propertyValuesIds.get(enhancedPropInstance.getId());
                for (final EntityContainer<E> thisPropertyEntity : thisPropertyEntities) {
                    // replace with parent container if types are the same (the cases of transitive linkage exist but are not supported here
                    if (enhancedPropInstance.getEntities().get(linkPropName).getResultType().equals(thisPropertyEntity.getResultType())) {
                        enhancedPropInstance.getEntities().put(linkPropName, thisPropertyEntity);
                    }
                    thisPropertyEntity.getEntities().put(propertyName, enhancedPropInstance);
                }
            }
        }

        return entities;
    }

    /**
     * @param entities  containers to enhance
     * @param propertyName  name of the property to enhance
     * @param fetchModel  fetch model for the property
     */
    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceProperty(final List<EntityContainer<E>> entities, final String propertyName, final EntityRetrievalModel<T> fetchModel, final Map<String, Object> paramValues) {
        // Obtaining map between property id and list of entities where this property occurs
        final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);

        if (!propertyValuesIds.isEmpty()) {
            // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
            final List<EntityContainer<T>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
            // IMPORTANT: it is assumed that EntityContainer can contain either only id or all props at once. Such assumption relied on fact that once join to property has been made all its columns had been yielded automatically.
            final List<EntityContainer<T>> enhancedPropInstances = retrievedPropertyInstances.isEmpty()
                    ? getDataInBatches(propertyValuesIds.keySet(), ID, fetchModel, paramValues)
                    : new EntityContainerEnhancer<T>(fetcher, domainMetadataAnalyser, idOnlyProxiedEntityTypeCache).enhance(retrievedPropertyInstances, fetchModel, paramValues);

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

    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceCollectional(final List<EntityContainer<E>> entitiesToBeEnhanced, final String propertyName, final Class propType, final String parentPropName, final EntityRetrievalModel<T> fetchModel, final Map<String, Object> paramValues) {
        // collect parental ids
        final Map<Long, EntityContainer<E>> parentIds = new HashMap<Long, EntityContainer<E>>();
        for (final EntityContainer<E> parentEntity : entitiesToBeEnhanced) {
            parentIds.put(parentEntity.getId(), parentEntity);
        }
        final List<EntityContainer<T>> properties = getDataInBatches(parentIds.keySet(), parentPropName, fetchModel, paramValues);

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
            if (SortedSet.class.equals(propType) || Set.class.equals(propType)) {
                entity.getCollections().put(propertyName, new CollectionContainer<T>(resultEntry.getValue()));
            } else {
                throw new UnsupportedOperationException("Fetching via models for collections of type [" + propType + "] is not yet supported.");
            }
        }

        return entitiesToBeEnhanced;
    }

    /**
     * Retrieves and enhances entity containers that match given entity IDs.
     * The type of retrieved entities is derived from the fetch model.
     *
     * @param idProp  name of the property against which IDs should be matched
     * @param fetchModel  fetch model to apply during retrieval
     */
    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getDataInBatches(
            final Collection<Long> ids, final String idProp, final EntityRetrievalModel<T> fetchModel, final Map<String, Object> paramValues)
    {
        // TODO need to optimise -- WagonClass in WagonClassCompatibility is re-retrieved, while already available
        // this ceremony with spliterators is needed to accept any Collection
        return StreamSupport.stream(spliterator(partition(ids, BATCH_SIZE).iterator(), BATCH_SIZE, NONNULL), false)
                // TODO remove once Collections are supported by EQL fluent API
                .map(batch -> batch.toArray(Long[]::new))
                .flatMap(batch -> {
                    final var model = select(fetchModel.getEntityType()).where().prop(idProp).in().values(batch).model();
                    final var qpm = new QueryProcessingModel<>(model, null, fetchModel, paramValues, false);
                    return fetcher.streamAndEnhanceContainers(qpm, Optional.empty()).flatMap(Collection::stream);
                }).toList();
    }

}
