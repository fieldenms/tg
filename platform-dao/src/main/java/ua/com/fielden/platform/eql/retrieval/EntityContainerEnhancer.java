package ua.com.fielden.platform.eql.retrieval;

import org.hibernate.Session;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.*;
import ua.com.fielden.platform.eql.retrieval.exceptions.EntityRetrievalException;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataUtils;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Iterables.partition;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliterator;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.findCollectionalProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

public class EntityContainerEnhancer {

    private static final Integer BATCH_SIZE = 990;
    // private static final Logger logger = getLogger(EntityContainerEnhancer.class);

    private final IEntityContainerFetcher fetcher;
    private final IDomainMetadata domainMetadata;
    private final PropertyMetadataUtils propMetadataUtils;
    private final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;

    protected EntityContainerEnhancer(
            final IEntityContainerFetcher fetcher,
            final IDomainMetadata domainMetadata,
            final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache)
    {
        this.fetcher = fetcher;
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;
        this.domainMetadata = domainMetadata;
        this.propMetadataUtils = domainMetadata.propertyMetadataUtils();
    }

    /**
     * Enhances a list of entity containers.
     * The entity type should be either a domain entity or {@link EntityAggregates}.
     */
    protected <E extends AbstractEntity<?>> List<EntityContainer<E>> enhance(
            final Session session,
            final List<EntityContainer<E>> entities,
            final IRetrievalModel<E> fetchModel, final Map<String, Object> paramValues)
    {
        if (entities.isEmpty() || fetchModel == null) {
            return entities;
        }

        fetchModel.getRetrievalModels().forEach((propName, propFetchModel) -> {
            if (propFetchModel.isFetchIdOnly()) {
                assignIdOnlyProxiedResultTypeToIdOnlyEntityProperty(entities, propName, propFetchModel.getEntityType());
            } else {
                if (fetchModel.getEntityType() != EntityAggregates.class) {
                    // @formatter:off
                    domainMetadata.forPropertyOpt(fetchModel.getEntityType(), propName)
                        .ifPresentOrElse(pm -> {
                             if (pm.type().isCollectional()) {
                                 enhanceCollectional(session, entities, fetchModel, paramValues, propName, propFetchModel);
                             }
                             else if (propMetadataUtils.isPropEntityType(pm.type(), EntityMetadata::isUnion)) {
                                 enhanceProperty(session, entities, propName, propFetchModel, paramValues);
                             }
                             else {
                                 try {
                                     final String linkPropName = Finder.findLinkProperty(fetchModel.getEntityType(), propName);
                                     enhancePropertyWithLinkToParent(session, entities, propName, propFetchModel, linkPropName, paramValues);
                                 } catch (final Exception e) {
                                     if (propMetadataUtils.isPropEntityType(pm.type(), em -> em.isPersistent() || em.isSynthetic() || EntityUtils.isOneToOne(em.javaType()))) {
                                         enhanceProperty(session, entities, propName, propFetchModel, paramValues);
                                     } else {
                                         // logger.debug(format("Property [%s] of type [%s] can't be fetched with model: %s.", propName, fetchModel.getEntityType(), entry.getValue()));
                                     }
                                 }
                             }
                         },
                         // TODO Why is absence of property metadata a valid condition? Platform tests show no such occurrences?
                         () -> enhanceCollectional(session, entities, fetchModel, paramValues, propName, propFetchModel));
                    // @formatter:on
                } else {
                    enhanceProperty(session, entities, propName, propFetchModel, paramValues);
                }
            }
        });

        assignProxiedResultTypeToContainers(entities, fetchModel);
        assignInstrumentationSetting(entities, fetchModel);

        return entities;
    }

    private <E extends AbstractEntity<?>> void enhanceCollectional(
            final Session session,
            final List<EntityContainer<E>> entities,
            final IRetrievalModel<E> fetchModel,
            final Map<String, Object> paramValues,
            final String propName,
            final EntityRetrievalModel<? extends AbstractEntity<?>> propFetchModel)
    {
        // TODO replace with EntityTree metadata (wrt collectional properties retrieval)
        // TODO if the property is missing, consider throwing an exception instead of silently doing nothing
        findCollectionalProperty(fetchModel.getEntityType(), propName).ifPresent(prop -> {
            final String linkPropName = Finder.findLinkProperty(fetchModel.getEntityType(), propName);
            enhanceCollectional(session, entities, propName, prop.getType(), linkPropName, propFetchModel, paramValues);
        });
    }

    private <E extends AbstractEntity<?>> void assignInstrumentationSetting(
            final List<EntityContainer<E>> entities, final IRetrievalModel<E> fetchModel)
    {
        if (fetchModel.isInstrumented()) {
            for (final EntityContainer<E> entityContainer : entities) {
                entityContainer.mkInstrumented();
            }
        }
    }

    private <T extends AbstractEntity<?>> Class<? extends T> determineProxiedResultTypeFromFetchModel(final IRetrievalModel<T> fetchModel) {
        // final DateTime st = new DateTime();
        final Class<? extends T> proxiedType = EntityProxyContainer.proxy(fetchModel.getEntityType(), fetchModel.getProxiedProps());
        // final Period pd = new Period(st, new DateTime());
        // logger.debug(format("Constructing proxy type [" + fetchModel.getEntityType().getSimpleName() + "] duration: %s m %s s %s ms.", pd.getMinutes(), pd.getSeconds(), pd.getMillis()));
        return proxiedType;
    }

    private <E extends AbstractEntity<?>> void assignProxiedResultTypeToContainers(
            final List<EntityContainer<E>> entities, final IRetrievalModel<E> fetchModel)
    {
        if (fetchModel.getEntityType() != EntityAggregates.class) {
            final Class<? extends E> proxiedResultType = determineProxiedResultTypeFromFetchModel(fetchModel);
            entities.forEach(entity -> entity.setProxiedResultType(proxiedResultType));
        }
    }

    private <E extends AbstractEntity<?>, T extends AbstractEntity<?>> void assignIdOnlyProxiedResultTypeToIdOnlyEntityProperty(
            final List<EntityContainer<E>> entities,
            final String propName, final Class<T> propType)
    {
        if (isUnionEntityType(propType)) {
            for (final var entity : entities) {
                final var unionContainer = entity.getEntities().get(propName);
                if (unionContainer != null) {
                    for (final var memberContainer : unionContainer.getEntities().values()) {
                        final var memberProxyType = idOnlyProxiedEntityTypeCache.getIdOnlyProxiedTypeFor(memberContainer.getResultType());
                        if (memberProxyType != null) {
                            memberContainer.setProxiedResultType((Class) memberProxyType);
                        }
                    }
                }
            }
        }
        else if (propType != EntityAggregates.class) {
            final Class<? extends T> proxiedPropType = idOnlyProxiedEntityTypeCache.getIdOnlyProxiedTypeFor(propType);
            for (final EntityContainer<E> entity : entities) {
                final EntityContainer<T> propContainer = (EntityContainer<T>) entity.getEntities().get(propName);
                if (propContainer != null) {
                    propContainer.setProxiedResultType(proxiedPropType);
                }
            }
        }
    }

    /**
     * Entity containers are scanned for inner entity containers (IEC) associated with the given property.
     * The result is a grouping of input containers by IDs of IEC.
     * Depth of scanning is limited to a single level.
     */
    private <E extends AbstractEntity<?>> Map<Long, List<EntityContainer<E>>> getEntityPropertyIds(
            final List<EntityContainer<E>> entities, final String propertyName)
    {
        return entities.stream()
                .peek(entity -> {
                    if (entity.isEmpty()) {
                        throw new EntityContainerEnhancementException(
                                """
                                Refusing to process empty entity container.
                                Property: %s
                                Container: %s\
                                """.formatted(propertyName, entity));
                    }
                })
                // (entityContainer, propertyContainer)
                .map(entity -> t2(entity, entity.getEntities().get(propertyName)))
                .filter(e_p -> e_p._2 != null && !e_p._2.isEmpty() && e_p._2.getId() != null)
                .collect(groupingBy(e_p -> e_p._2.getId(), mapping(e_p -> e_p._1, toList())));
    }

    private <E extends AbstractEntity<?>, T extends AbstractEntity<?>> List<EntityContainer<T>> getRetrievedPropertyInstances(
            final List<EntityContainer<E>> entities, final String propName) {
        return entities.stream()
                .map(entity -> (EntityContainer<T>) entity.getEntities().get(propName))
                .filter(prop -> prop != null && !prop.isEmpty() && !prop.notYetInitialised())
                .toList();
    }

    private <E extends AbstractEntity<?> ,T extends AbstractEntity<?>> List<EntityContainer<E>> enhancePropertyWithLinkToParent(
            final Session session,
            final List<EntityContainer<E>> entities,
            final String propertyName,
            final EntityRetrievalModel<T> fetchModel,
            final String linkPropName,
            final Map<String, Object> paramValues)
    {
        // Obtaining map between property id and list of entities where this property occurs
        final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);

        if (!propertyValuesIds.isEmpty()) {
            // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
            final List<EntityContainer<T>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
            // IMPORTANT: it is assumed that EntityContainer can contain either only id or all props at once.
            //            Such assumption relies on the fact that once a join to a property has been made, all its columns are yielded automatically.
            final List<EntityContainer<T>> enhancedPropInstances = retrievedPropertyInstances.isEmpty()
                    ? getDataInBatches(session, propertyValuesIds.keySet(), ID, fetchModel, paramValues)
                    : enhance(session, retrievedPropertyInstances, fetchModel, paramValues);

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
    private <E extends AbstractEntity<?>, T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceProperty(
            final Session session,
            final List<EntityContainer<E>> entities,
            final String propertyName,
            final EntityRetrievalModel<T> fetchModel,
            final Map<String, Object> paramValues)
    {
        // Obtaining map between property id and list of entities where this property occurs
        final Map<Long, List<EntityContainer<E>>> propertyValuesIds = getEntityPropertyIds(entities, propertyName);

        if (!propertyValuesIds.isEmpty()) {
            // Constructing model for retrieving property instances based on the provided fetch model and list of instances ids
            final List<EntityContainer<T>> retrievedPropertyInstances = getRetrievedPropertyInstances(entities, propertyName);
            // IMPORTANT: it is assumed that EntityContainer can contain either only id or all props at once. Such assumption relied on fact that once join to property has been made all its columns had been yielded automatically.
            final List<EntityContainer<T>> enhancedPropInstances = retrievedPropertyInstances.isEmpty()
                    ? getDataInBatches(session, propertyValuesIds.keySet(), ID, fetchModel, paramValues)
                    : enhance(session, retrievedPropertyInstances, fetchModel, paramValues);

            // Replacing in entities the proxies of properties with properly enhanced property instances.
            for (final var enhancedPropInstance : enhancedPropInstances) {
                final var thisPropertyEntities = propertyValuesIds.get(enhancedPropInstance.getId());
                thisPropertyEntities.forEach(thisPropertyEntity -> thisPropertyEntity.getEntities().put(propertyName, enhancedPropInstance));
            }
        }

        return entities;
    }

    /**
     * Enhances entity containers by populating collectional property {@code collPropName}.
     * This method mutates the containers it is given.
     * <p>
     * There are two entities at play here:
     * <ol>
     * <li> main entity (<i>ME</i>) -- the one being enhanced and owning the collectional property;
     * <li> detail entity (<i>DE</i>) -- the type of the collectional property's elements;
     * </ol>
     *
     * Both ME and DE containers are enhanced by populating the respective sides of their one-2-many relationship.
     *
     * @param masterEntities  ME containers that will be enhanced
     * @param collPropName  name of the collectional property
     * @param collPropType  type of the collectional property
     * @param linkPropName  name of the link property, i.e., property of DE typed with ME
     * @param fetchModel  fetch model for DE
     * @param paramValues  query parameters for DE
     *
     * @return  ME containers enhanced with collectional property by populating it with DE containers, which are also
     *          enhanced by populating the link property with the corresponding ME
     */
    private <E extends AbstractEntity<?>, T extends AbstractEntity<?>> List<EntityContainer<E>> enhanceCollectional(
            final Session session,
            final List<EntityContainer<E>> masterEntities,
            final String collPropName, final Class<?> collPropType,
            final String linkPropName,
            final EntityRetrievalModel<T> fetchModel,
            final Map<String, Object> paramValues)
    {
        final Map<Long, EntityContainer<E>> idToMaster = masterEntities.stream()
                .collect(toImmutableMap(EntityContainer::getId, Function.identity()));
        // DE containers where the value of link property is contained in master IDs
        final List<EntityContainer<T>> details = getDataInBatches(session, idToMaster.keySet(), linkPropName, fetchModel, paramValues);
        // group DE containers by master's id
        final Map<Long, List<EntityContainer<T>>> masterIdToDetails = details.stream()
                .collect(groupingBy(det -> det.getEntities().get(linkPropName).getId()));
        // mutate DE containers by assigning the corresponding ME container instance
        masterIdToDetails.forEach((masterId, dets) ->
                                    dets.forEach(det -> det.getEntities().put(linkPropName, idToMaster.get(masterId))));

        if (!SortedSet.class.equals(collPropType) && !Set.class.equals(collPropType)) {
            throw new EntityRetrievalException(
                    "Fetching of collectional property type [%s] is not supported.".formatted(collPropType.getTypeName()));
        }

        // populate ME containers with associated DE containers
        masterIdToDetails.forEach((masterId, dets) ->
                                    idToMaster.get(masterId).getCollections().put(collPropName, new CollectionContainer<>(dets)));

        return masterEntities;
    }

    /**
     * Retrieves and enhances entity containers that match given entity IDs.
     * The type of retrieved entities is derived from the fetch model.
     *
     * @param ids  ID values for entities to be retrieved.
     * @param idProp  name of the property against which IDs should be matched.
     * @param fetchModel  fetch model to apply during retrieval.
     * @param paramValues  query parameters.
     */
    private <T extends AbstractEntity<?>> List<EntityContainer<T>> getDataInBatches(
            final Session session,
            final Collection<Long> ids,
            final String idProp,
            final EntityRetrievalModel<T> fetchModel,
            final Map<String, Object> paramValues)
    {
        // TODO need to optimise -- WagonClass in WagonClassCompatibility is re-retrieved, while already available
        // This ceremony with spliterators is needed to accept any Collection
        return StreamSupport.stream(spliterator(partition(ids, BATCH_SIZE).iterator(), BATCH_SIZE, NONNULL), false)
                .flatMap(batch -> {
                    final var model = select(fetchModel.getEntityType()).where().prop(idProp).in().values(batch).model();
                    final var qpm = new QueryProcessingModel<>(model, null, fetchModel, paramValues, false);
                    return fetcher.streamAndEnhanceContainers(session, qpm, Optional.empty()).flatMap(Collection::stream);
                }).toList();
    }
    
}
