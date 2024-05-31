package ua.com.fielden.platform.eql.retrieval;

import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.query.*;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataUtils;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.*;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.findCollectionalProperty;

public class EntityContainerEnhancer<E extends AbstractEntity<?>> {
    private final EntityContainerFetcher fetcher;
    private final IDomainMetadata domainMetadata;
    private final PropertyMetadataUtils propMetadataUtils;
    private final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;
    private static final Logger logger = getLogger(EntityContainerEnhancer.class);

    protected EntityContainerEnhancer(final EntityContainerFetcher fetcher, final IDomainMetadata domainMetadata, final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        this.fetcher = fetcher;
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;
        this.domainMetadata = domainMetadata;
        this.propMetadataUtils = domainMetadata.propertyMetadataUtils();
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
                    // @formatter:off
                    domainMetadata.forProperty(fetchModel.getEntityType(), propName)
                        .ifPresentOrElse(pm -> {
                             if (pm.type().isCollectional()) {
                                 enhanceCollectional(entities, fetchModel, paramValues, propName, propFetchModel);
                             }
                             else if (propMetadataUtils.isPropEntityType(pm.type(), em -> em.nature().isUnion())) {
                                 enhanceProperty(entities, propName, propFetchModel, paramValues);
                             }
                             else {
                                 try {
                                     final String linkPropName = Finder.findLinkProperty(fetchModel.getEntityType(), propName);
                                     enhancePropertyWithLinkToParent(entities, propName, propFetchModel, linkPropName, paramValues);
                                 } catch (final Exception e) {
                                     if (propMetadataUtils.isPropEntityType(pm.type(), em -> em.nature().isPersistent() || EntityUtils.isOneToOne(em.javaType()))) {
                                         enhanceProperty(entities, propName, propFetchModel, paramValues);
                                     } else {
                                         // logger.debug(format("Property [%s] of type [%s] can't be fetched with model: %s.", propName, fetchModel.getEntityType(), entry.getValue()));
                                     }
                                 }
                             }
                         },
                         // TODO Why is absence of property metadata a valid condition? Platform tests show no such occurences
                         () -> enhanceCollectional(entities, fetchModel, paramValues, propName, propFetchModel));
                    // @formatter:on
                } else {
                    enhanceProperty(entities, propName, propFetchModel, paramValues);
                }
            }
        }

        assignProxiedResultTypeToContainers(entities, fetchModel);
        assignInstrumentationSetting(entities, fetchModel);

        return entities;
    }

    private void enhanceCollectional(final List<EntityContainer<E>> entities, final IRetrievalModel<E> fetchModel, final Map<String, Object> paramValues, final String propName, final EntityRetrievalModel<? extends AbstractEntity<?>> propFetchModel) {
        // TODO replace with EntityTree metadata (wrt collectional properties retrieval)
        // TODO if the property is missing, consider throwing an exception instead of silently doing nothing
        findCollectionalProperty(fetchModel.getEntityType(), propName).ifPresent(prop -> {
            final String linkPropName = Finder.findLinkProperty(fetchModel.getEntityType(), propName);
            enhanceCollectional(entities, propName, prop.getType(), linkPropName, propFetchModel, paramValues);
        });
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
            final List<EntityContainer<T>> enhancedPropInstances = (retrievedPropertyInstances.size() == 0) ? //
            getDataInBatches(new ArrayList<Long>(propertyValuesIds.keySet()), fetchModel, paramValues)
                    : //
                    new EntityContainerEnhancer<T>(fetcher, domainMetadata, idOnlyProxiedEntityTypeCache).enhance(retrievedPropertyInstances, fetchModel, paramValues);

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

    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceProperty(final List<EntityContainer<E>> entities, final String propertyName, final EntityRetrievalModel<T> fetchModel, final Map<String, Object> paramValues) {
        // Obtaining map between property id and list of entities where this property occurs
        final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);

        if (propertyValuesIds.size() > 0) {
            // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
            final List<EntityContainer<T>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
            // IMPORTANT: it is assumed that EntityContainer can contain either only id or all props at once. Such assumption relied on fact that once join to property has been made all its columns had been yielded automatically.
            final List<EntityContainer<T>> enhancedPropInstances = (retrievedPropertyInstances.size() == 0) ? //
            getDataInBatches(new ArrayList<Long>(propertyValuesIds.keySet()), fetchModel, paramValues)
                    : //
                    new EntityContainerEnhancer<T>(fetcher, domainMetadata, idOnlyProxiedEntityTypeCache).enhance(retrievedPropertyInstances, fetchModel, paramValues);

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

    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getDataInBatches(final List<Long> ids, final EntityRetrievalModel<T> fetchModel, final Map<String, Object> paramValues) {
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
            final QueryProcessingModel<T, ?> qpm = new QueryProcessingModel<>(currTypePropertyModel, null, fetchModel, paramValues/*emptyMap()*/, false);
            final List<EntityContainer<T>> properties = fetcher.listAndEnhanceContainers(qpm, null, null);
            result.addAll(properties);
            from = to;
            to = to + batchSize;
        }

        return result;
    }

    /**
     * Enhances entity containers by populating collectional property {@code collPropName}.
     * This method mutates the containers it is given.
     * <p>
     * There are 2 entities at play here:
     * <ol>
     * <li> master entity (<i>ME</i>) -- the one being enhanced and owning the collectional property;
     * <li> detail entity (<i>DE</i>) -- the type of the collectional property's elements;
     * </ol>
     *
     * Both ME and DE containers are enhanced by populating the respective sides of their one-to-many relationhsip.
     *
     * @param masterEntities  ME containers that will be enhanced
     * @param collPropName  name of the collectional property
     * @param collPropType  type of the collectional property
     * @param linkPropName  name of the link property, i.e., property of DE typed with ME
     * @param fetchModel  fetch model for DE
     * @param paramValues  query parameters for DE
     *
     * @return  ME containers enhanced with collectional property by populating it with DE containers which are also
     *          enhanced by populating the link property with the corresponding ME
     */
    private <T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceCollectional
            (final List<EntityContainer<E>> masterEntities,
             final String collPropName, final Class<?> collPropType,
             final String linkPropName,
             final EntityRetrievalModel<T> fetchModel,
             final Map<String, Object> paramValues)
    {
        final Map<Long, EntityContainer<E>> idToMaster = masterEntities.stream()
                .collect(toImmutableMap(EntityContainer::getId, Function.identity()));
        // DE containers where the value of link property is contained in master IDs
        final List<EntityContainer<T>> details = getCollectionalDataInBatches(idToMaster.keySet(), linkPropName, fetchModel, paramValues);
        // group DE containers by master's id
        final Map<Long, List<EntityContainer<T>>> masterIdToDetails = details.stream()
                .collect(groupingBy(det -> det.getEntities().get(linkPropName).getId()));
        // mutate DE containers by assigning the corresponding ME container instance
        masterIdToDetails.forEach((masterId, dets) ->
                                    dets.forEach(det -> det.getEntities().put(linkPropName, idToMaster.get(masterId))));

        if (!(SortedSet.class.equals(collPropType) || Set.class.equals(collPropType))) {
            throw new UnsupportedOperationException(
                    "Fetching of collectional property type [%s] is not supported.".formatted(collPropType.getTypeName()));
        }

        // populate ME containers with associated DE containers
        masterIdToDetails.forEach((masterId, dets) ->
                                    idToMaster.get(masterId).getCollections().put(collPropName, new CollectionContainer<>(dets)));

        return masterEntities;
    }

    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getCollectionalDataInBatches(final Set<Long> parentIds, final String parentPropName, final EntityRetrievalModel<T> fetchModel, final Map<String, Object> paramValues) {
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
            final QueryProcessingModel<T, ?> qpm = new QueryProcessingModel<>(currTypePropertyModel, null, fetchModel, paramValues, false);
            final List<EntityContainer<T>> properties = fetcher.listAndEnhanceContainers(qpm, null, null);
            result.addAll(properties);
            // TODO need to optimise -- WagonClass in WagonClassCompatibility is re-retrieved, while already available
            from = to;
            to = to + batchSize;
        }

        return result;
    }
}
