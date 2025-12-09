package ua.com.fielden.platform.eql.meta;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.query.*;
import ua.com.fielden.platform.eql.meta.utils.DependentCalcPropsOrder;
import ua.com.fielden.platform.eql.meta.utils.TopologicalSortException;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.AbstractQuery1;
import ua.com.fielden.platform.eql.stage1.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage1.sources.YieldInfoNodesGenerator;
import ua.com.fielden.platform.eql.stage2.queries.SourceQuery2;
import ua.com.fielden.platform.meta.*;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.util.Collections.emptySortedMap;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.concat;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.eql.meta.utils.TopologicalSort.sortTopologically;
import static ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnQueries.ERR_YIELD_INTO_NON_EXISTENT_PROPERTY;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_ENTITY;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.utils.CollectionUtil.mapValues;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.StreamUtils.distinct;

/// An abstraction for EQL-specific metadata about query sources (i.e. entity types).
///
/// The term for this metadata is "query source info", and it is organised using 2 categories:
/// -  **Declared** - contains a subset of properties present in the corresponding [entity metadata][ua.com.fielden.platform.reflection.EntityMetadata].
///     Only properties that are relevant to EQL are included.
/// -  **Modelled** - depends on the entity's nature:
///     -  Persistent, Union - equal to the declared query source info.
///     -  Synthetic - contains neither a superset, nor a subset of properties present in the entity's metadata.
///        Instead, it includes only those properties that are yielded in the underlying models.
///
@Singleton
public class QuerySourceInfoProvider {

    private static final Logger LOGGER = getLogger(QuerySourceInfoProvider.class);

    public static final String ERR_CONFLICT_BETWEEN_YIELDED_AND_DECLARED_PROP_TYPE =
    """
    Could not determine type for property [%s] in a source query with source type [%s].
    Declared type: [%s]. Actual yield type: [%s]."\
    """;
    private static final String ERR_NON_RETRIEVABLE_PROP_YIELDED_WITH_DOT_EXPRESSION =
    "Non-retrievable property [%s] cannot be used as a dot-notated yield alias (in a source query with source type [%s]).\n";
    private static final String ERR_MISSING_CALC_PROPS_ORDER =
    """
    Analysis of dependent calculated properties wasn't performed for entity type [%s]. \
    This could indicate either an unregistered domain type or a generated type with added dependent calculated properties, which isn't supported.\
    """;
    public static final String ERR_FAILED_GENERATION_FOR_SYNTHETIC_ENTITY = "Could not generate modelled entity info for synthetic entity [%s].";

    /** Used to obtain models for synthetic entities. */
    private static final QueryModelToStage1Transformer QUERY_MODEL_TO_STAGE_1_TRANSFORMER = new QueryModelToStage1Transformer();
    public static final String WARN_GENERATING_MODELS_FOR_SYNTHETIC_ENTITY_MAY_AFFECT_PERFORMANCE = "Generating models for synthetic entity [%s] on demand. This may affect performance if attempted frequently.";
    public static final String ERR_EXPECTED_SYNTHETIC_ENTITY = "Expected a synthetic entity type, but was: %s";

    /** Association between an entity type and its declared query source info. */
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> declaredQuerySourceInfoMap;

    /** Association between an entity type and its modelled query source info. */
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> modelledQuerySourceInfoMap;

    /** Association between a synthetic entity type (SE) and its underlying models transformed to stage 1. */
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, List<SourceQuery1>> seModels;

    private final ConcurrentMap<String, List<String>> entityTypesDependentCalcPropsOrder;
    private final IDomainMetadata domainMetadata;
    private final ISyntheticModelProvider synModelProvider;

    @Inject
    public QuerySourceInfoProvider(
            final IDomainMetadata domainMetadata,
            final IDomainMetadataUtils domainMetadataUtils,
            final ISyntheticModelProvider synModelProvider)
    {
        this.domainMetadata = domainMetadata;
        this.synModelProvider = synModelProvider;

        // Declared query source infos are created for all entities.
        declaredQuerySourceInfoMap = domainMetadataUtils.registeredEntities()
                .collect(toConcurrentMap(EntityMetadata::javaType, em -> new QuerySourceInfo<>(em.javaType(), true)));
        declaredQuerySourceInfoMap.values()
                .forEach(ei -> ei.addProps(generateQuerySourceItems(declaredQuerySourceInfoMap, ei.javaType())));

        // Modelled query source infos require a bit more work for synthetic entities, but for other entities are the same as the declared ones.
        // Models of union entities are implicitly generated and have no interdependencies.
        // Thus, they don't require any extra work like synthetic models do.
        // Map `modelledQuerySourceInfoMap` needs to be mutable so that it can be used while we are populating it.
        // This constructor is quite complex, passing `this` to other parts of the system.
        modelledQuerySourceInfoMap = new ConcurrentHashMap<>(declaredQuerySourceInfoMap.size());

        // 1. Reuse declared query source infos for non-synthetic entities.
        // These must be created first so that they can be used during processing of synthetic types.
        declaredQuerySourceInfoMap.forEach((entityType, declaredQsi) -> {
            if (!domainMetadata.forEntity(entityType).isSynthetic()) {
                modelledQuerySourceInfoMap.put(entityType, declaredQsi);
            }
        });

        // 2. Create modelled query source infos for synthetic entities by analysing their underlying models.
        // Transform underlying models of synthetic entities to stage 1.
        seModels = domainMetadataUtils.registeredEntities()
                .map(EntityMetadata::asSynthetic).flatMap(Optional::stream)
                .collect(toConcurrentMap(EntityMetadata::javaType,
                                         em -> synModelProvider.getModels(em.javaType())
                                                 .stream()
                                                 .map(QUERY_MODEL_TO_STAGE_1_TRANSFORMER::generateAsUncorrelatedSourceQuery)
                                                 .toList()));
        // Compute dependencies between synthetic entities.
        final var seDependencies = mapValues(seModels,
                                             (type, queries) -> queries.stream()
                                                     .map(AbstractQuery1::collectEntityTypes)
                                                     .flatMap(Set::stream)
                                                     .filter(EntityUtils::isSyntheticEntityType)
                                                     .collect(toSet()));
        // Topological sorting will uncover any circular dependencies by throwing an exception.
        try {
            for (final var seType : sortTopologically(seDependencies)) {
                try {
                    final var modelledQuerySourceInfo = generateModelledQuerySourceInfoForSyntheticType(seType, seModels.get(seType));
                    modelledQuerySourceInfoMap.put(modelledQuerySourceInfo.javaType(), modelledQuerySourceInfo);
                } catch (final Exception ex) {
                    final var msg = ERR_FAILED_GENERATION_FOR_SYNTHETIC_ENTITY.formatted(seType.getTypeName());
                    LOGGER.error(msg, ex);
                    throw new EqlMetadataGenerationException(msg, ex);
                }
            }
        } catch (final TopologicalSortException $) {
            final var msg = "There are cyclic dependencies between synthetic entities. All dependencies:\n" +
                            seDependencies.entrySet().stream()
                                    .map(entry -> "%s depends on [%s]".formatted(entry.getKey().getSimpleName(),
                                                                                 CollectionUtil.toString(entry.getValue(), Class::getSimpleName, ", ")))
                                    .collect(joining("\n"));
            LOGGER.error(msg);
            throw new EqlMetadataGenerationException(msg);
        }
        // All modelled query source infos have been created.

        entityTypesDependentCalcPropsOrder = modelledQuerySourceInfoMap.values().stream()
                .collect(toConcurrentMap(querySourceInfo -> querySourceInfo.javaType().getName(),
                                         querySourceInfo -> DependentCalcPropsOrder.orderDependentCalcProps(this, domainMetadata, QUERY_MODEL_TO_STAGE_1_TRANSFORMER, querySourceInfo)));
    }

    /// Produces a query source info for the specified entity type backed by the specified models.
    ///
    /// Use cases of this method are:
    /// -  A synthetic entity type backed by its underlying models (as defined in the class).
    /// -  A persistent entity type backed by ad-hoc models used in a query (e.g. a union of 2 sub-queries that select from `Vehicle`).
    /// -  [EntityAggregates] backed by ad-hoc models used in a query.
    ///
    public <T extends AbstractEntity<?>> QuerySourceInfo<T> produceQuerySourceInfoForEntityType(
            final List<SourceQuery2> models,
            final Class<T> sourceType,
            final boolean isComprehensive)
    {
        final Map<String, AbstractQuerySourceItem<?>> declaredProps = EntityAggregates.class == sourceType
                ? emptySortedMap()
                : getDeclaredQuerySourceInfo(sourceType).getProps();

        final Stream<AbstractQuerySourceItem<?>> yieldedProps = YieldInfoNodesGenerator.generate(models).stream()
                .map(yield -> {
                    final var declaredProp = declaredProps.get(yield.name());
                    if (declaredProp != null) {
                        // The only thing that has to be taken from declared is its structure (in case of union or component-typed property).
                        if (declaredProp instanceof QuerySourceItemForEntityType<?> declaredEntityTypeQuerySourceInfoItem) {
                            if (yield.propType().isNull() ||
                                yield.propType().javaType() == declaredEntityTypeQuerySourceInfoItem.javaType() ||
                                // Long indicates ID is being yielded.
                                yield.propType().javaType() == Long.class)
                            {
                                return new QuerySourceItemForEntityType<>(yield.name(),
                                                                          getModelledQuerySourceInfo((Class<? extends AbstractEntity<?>>) declaredProp.javaType()),
                                                                          declaredEntityTypeQuerySourceInfoItem.hibType,
                                                                          yield.nonnullable());
                            } else {
                                throw new EqlStage1ProcessingException(
                                        format(ERR_CONFLICT_BETWEEN_YIELDED_AND_DECLARED_PROP_TYPE,
                                               declaredEntityTypeQuerySourceInfoItem.name, sourceType.getName(),
                                               declaredEntityTypeQuerySourceInfoItem.javaType().getName(),
                                               yield.propType().javaType().getName()));
                            }
                        } else {
                            // TODO need to ensure that in case of UE or complex value all declared subprops match yielded ones.
                            // TODO need actual (based on yield) rather than declared info (similar to not declared props section below).
                            return declaredProp.hasExpression() ? declaredProp.cloneWithoutExpression() : declaredProp;
                        }
                    }
                    else {
                        // This yield is using a non-declared property as an alias.
                        // Most likely, this is the case of EntityAggregates.
                        // Otherwise, this could indicate an invalid query.

                        if (sourceType != EntityAggregates.class) {
                            // Verify that the property exists in the entity type.
                            // This could be valid if a non-retrievable property (e.g., crit-only) is yielded into.
                            if (!domainMetadata.forEntity(sourceType).hasProperty(yield.name())) {
                                throw new EqlStage1ProcessingException(ERR_YIELD_INTO_NON_EXISTENT_PROPERTY.formatted(yield.name(), sourceType.getSimpleName()));
                            }
                        }

                        if (yield.propType() == null) {
                            // yield.propType() can be null if the yield uses a dot-expression as an alias (e.g., "price.amount").
                            // Effectively, this disables the use of such aliases in a source query with modelAsAggregate().
                            // The source type could also be an entity type other than EntityAggregates.
                            // This would indicate an invalid yield that uses a non-retrievable (sub-)property as an alias.
                            // TODO: Support this if necessary.
                            throw new EqlStage1ProcessingException(ERR_NON_RETRIEVABLE_PROP_YIELDED_WITH_DOT_EXPRESSION.formatted(yield.name(), sourceType.getSimpleName()));
                        } else {
                            return yield.propType().isNotNull() && isEntityType(yield.propType().javaType())
                                    ? new QuerySourceItemForEntityType<>(yield.name(),
                                                                         getModelledQuerySourceInfo((Class<? extends AbstractEntity<?>>) yield.propType().javaType()),
                                                                         H_ENTITY,
                                                                         yield.nonnullable())
                                    : new QuerySourceItemForPrimType<>(yield.name(),
                                                                       yield.propType().isNotNull() ? yield.propType().javaType() : null,
                                                                       yield.propType().isNotNull() ? yield.propType().hibType() : null);
                        }
                    }
                });

        // Include all calculated properties, which haven't been yielded explicitly.
        final var calculatedProps = declaredProps.values().stream()
                .filter(AbstractQuerySourceItem::hasExpression);

        final var allProps = distinct(concat(yieldedProps, calculatedProps), qsi -> qsi.name)
                .collect(toImmutableList());

        return new QuerySourceInfo<>(sourceType, isComprehensive, allProps);
    }

    /// Only properties that are present in SE yields are preserved.
    ///
    private <T extends AbstractEntity<?>> QuerySourceInfo<?> generateModelledQuerySourceInfoForSyntheticType(final Class<? extends AbstractEntity<?>> entityType, final List<SourceQuery1> queries) {
        final TransformationContextFromStage1To2 context = TransformationContextFromStage1To2.forMainContext(this, domainMetadata);
        final List<SourceQuery2> transformedQueries = queries.stream().map(m -> m.transform(context)).collect(toList());
        return produceQuerySourceInfoForEntityType(transformedQueries, entityType, true /*isComprehensive*/);
    }

    public List<String> getCalcPropsOrder(final Class<? extends AbstractEntity<?>> entityType) {
        // TODO: It is assumed that there would be no generated types with newly added dependent calc props.
        //       This assumption needs to be revisited when implementing support for user-definable calculated properties.
        final var order = entityTypesDependentCalcPropsOrder.get(getOriginalType(entityType).getName());
        if (order == null) {
            throw new EqlMetadataGenerationException(ERR_MISSING_CALC_PROPS_ORDER.formatted(entityType.getSimpleName()));
        } else {
            return order;
        }
    }

    private List<AbstractQuerySourceItem<?>> generateQuerySourceItems(
            final Map<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> allQuerySourceInfos,
            final Class<? extends AbstractEntity<?>> entityType)
    {
        final var pmUtils = domainMetadata.propertyMetadataUtils();
        final var entityMetadata = domainMetadata.forEntity(entityType);


        // Exclude properties that have no meaning from the persistence perspective.
        // In other words, values for such properties cannot be retrieved from a database.
        // Effectively, for persistent entities, only calculated and persistent properties can be retrieved.
        // Properties of any other nature are considered such that do not have anything to do with persistence.
        // For synthetic entities, properties of any nature can be retrieved as long as they are yielded or can be calculated.
        // This is why it is considered that such entities do not have "pure" properties that cannot be retrieved.
        // Although, it is possible to declare a plain property and not use it in the model for yielding.
        // Attempts to specify such properties in a fetch model when retrieving a synthetic entity should result in a runtime exception.
        return entityMetadata.properties().stream()
            .filter(pm -> !pm.isCritOnly())
            .filter(pm -> !(pm.isPlain() && entityMetadata.isPersistent()))
            .<Optional<AbstractQuerySourceItem<?>>> map(pm -> {
                final var name = pm.name();
                final var hibType = pm.hibType();
                final @Nullable var expr = pm.asCalculated().map(QuerySourceInfoProvider::toCalcPropInfo).orElse(null);

                return switch (pm.type()) {
                    case PropertyTypeMetadata.Entity et -> mkQuerySourceItemForEntityType(pm, et, allQuerySourceInfos);
                    case PropertyTypeMetadata.Component ct -> {
                        final var propTpi = new QuerySourceItemForComponentType<>(name, ct.javaType(), hibType);
                        for (final PropertyMetadata spm : pmUtils.subProperties(pm)) {
                            propTpi.addSubitem(
                                    new QuerySourceItemForPrimType<>(spm.name(), (Class<?>) spm.type().javaType(),
                                                                     spm.hibType(),
                                                                     spm.asCalculated().map(QuerySourceInfoProvider::toCalcPropInfo).orElse(null)));
                        }
                        yield Optional.of(propTpi);
                    }
                    case PropertyTypeMetadata.CompositeKey ckt ->
                            Optional.of(new QuerySourceItemForPrimType<>(name, ckt.javaType(), hibType, expr));
                    case PropertyTypeMetadata.Primitive pt ->
                            Optional.of(new QuerySourceItemForPrimType<>(name, pt.javaType(), hibType, expr));
                    default -> Optional.empty();
                };
            })
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<AbstractQuerySourceItem<?>> mkQuerySourceItemForEntityType(
            final PropertyMetadata pm,
            final PropertyTypeMetadata.Entity et,
            final Map<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> allQuerySourceInfos)
    {
        return domainMetadata.forEntityOpt(et.javaType())
                .map(em -> switch (em) {
                    case EntityMetadata.Union uem -> mkQuerySourceItemForUnionEntityType(pm, uem, allQuerySourceInfos);
                    case EntityMetadata.Persistent pem ->
                        // TODO: The following commented out code may potentially be used for future improvements related to treating ID property as if it is an entity
                        //       while yielding ID property within an EntityAggregates result model.
                        // if (ID.equals(name))
                        //     querySourceInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(querySourceInfo.javaType()), hibType, required, expr));
                            new QuerySourceItemForEntityType<>(pm.name(), allQuerySourceInfos.get(pem.javaType()), pm.hibType(), pm.is(REQUIRED),
                                                               pm.asCalculated().map(QuerySourceInfoProvider::toCalcPropInfo).orElse(null));
                    default ->
                        // TODO: It is not clear why QuerySourceItemForPrimType is used in all other cases.
                        //       The original comment here was:
                        //       // Finally, if nothing else, the property must be of some primitive type or a type with a custom Hibernate converter:
                        //       // String, Long, Integer, BigDecimal, Date, boolean, Colour, Hyperlink, PropertyDescriptor, SecurityToken.
                        //       However, this case would also include properties of Synthetic Entity type, but such properties are recognised as transient (plain) and later ignored.
                        //       Properties of Value Entity type are not handled by this case.
                        //       This is because, domainMetadata.forEntityOpt(PropertyDescriptor.class) returns an empty result, and Union Entities are handled separately above.
                            mkPrim(pm);
                })
                // TODO: The empty case seems to handle only properties of type PropertyDescriptor, which is a Value Entity rather than a primitive property.
                //       Need to investigate this further.
                .or(() -> Optional.of(mkPrim(pm)));
    }
    // where
    private QuerySourceItemForPrimType<?> mkPrim(final PropertyMetadata pm) {
        return new QuerySourceItemForPrimType<>(pm.name(), (Class<?>) pm.type().javaType(), pm.hibType(),
                                                pm.asCalculated().map(QuerySourceInfoProvider::toCalcPropInfo).orElse(null));
    }

    private AbstractQuerySourceItem<?> mkQuerySourceItemForUnionEntityType
        (final PropertyMetadata pm, final EntityMetadata.Union em,
         final Map<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> allQuerySourceInfos)
    {
        final SortedMap<String, AbstractQuerySourceItem<?>> subprops = new TreeMap<>();
        for (final PropertyMetadata spm : domainMetadata.propertyMetadataUtils().subProperties(pm)) {
            switch (spm) {
                case PropertyMetadata.Calculated cspm -> {
                    switch (cspm.type()) {
                        case PropertyTypeMetadata.Entity t ->
                                subprops.put(cspm.name(), new QuerySourceItemForEntityType<>(cspm.name(), allQuerySourceInfos.get(t.javaType()),
                                                                                             cspm.hibType(), false,
                                                                                             toCalcPropInfo(cspm)));
                        case PropertyTypeMetadata.Component t ->
                                subprops.put(cspm.name(), new QuerySourceItemForPrimType<>(cspm.name(), t.javaType(),
                                                                                           cspm.hibType(), toCalcPropInfo(cspm)));
                        case PropertyTypeMetadata.CompositeKey t ->
                                subprops.put(cspm.name(), new QuerySourceItemForPrimType<>(cspm.name(), t.javaType(),
                                                                                           cspm.hibType(), toCalcPropInfo(cspm)));
                        case PropertyTypeMetadata.Primitive t ->
                                subprops.put(cspm.name(), new QuerySourceItemForPrimType<>(cspm.name(), t.javaType(),
                                                                                           cspm.hibType(), toCalcPropInfo(cspm)));
                        default -> {}
                    }
                }
                default -> {
                    // non-calculated properties in a union entity should be the union members, which are always entity-typed
                    switch (spm.type()) {
                        case PropertyTypeMetadata.Entity subEt ->
                                subprops.put(spm.name(), new QuerySourceItemForEntityType<>(spm.name(),
                                                                                            allQuerySourceInfos.get(subEt.javaType()),
                                                                                            spm.hibType(), false, null));
                        default -> {}
                    }
                }
            }
        }
        return new QuerySourceItemForUnionType<>(pm.name(), em.javaType(), pm.hibType(), subprops);
    }

    private QuerySourceInfo<?> generateDeclaredQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        return new QuerySourceInfo<>(type, true, generateQuerySourceItems(declaredQuerySourceInfoMap, type));
    }

    private QuerySourceInfo<?> generateModelledQuerySourceInfoForPersistentType(final Class<? extends AbstractEntity<?>> type) {
        return new QuerySourceInfo<>(type, true, generateQuerySourceItems(modelledQuerySourceInfoMap, type));
    }

    /// @param type  entity type which must be reifiable with metadata
    ///
    public QuerySourceInfo<?> getDeclaredQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        final QuerySourceInfo<?> existing = declaredQuerySourceInfoMap.get(type);
        return existing != null ? existing : generateDeclaredQuerySourceInfo(type);
    }

    /// @param type  entity type which must be reifiable with metadata
    ///
    public QuerySourceInfo<?> getModelledQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        final QuerySourceInfo<?> existing = modelledQuerySourceInfoMap.get(type);
        if (existing != null) {
            return existing;
        }

        if (domainMetadata.forEntity(type) instanceof EntityMetadata.Synthetic) {
            return generateModelledQuerySourceInfoForSyntheticType(type, getSeModels(getOriginalType(type)));
        }
        return generateModelledQuerySourceInfoForPersistentType(type);
    }

    private List<SourceQuery1> getSeModels(final Class<? extends AbstractEntity<?>> entityType) {
        final var models = seModels.get(getOriginalType(entityType));
        if (models != null) {
            return models;
        } else {
            // This branch is intended to be executed by platform tests, allowing to use entity types without registering them in the application domain.
            LOGGER.warn(() -> WARN_GENERATING_MODELS_FOR_SYNTHETIC_ENTITY_MAY_AFFECT_PERFORMANCE.formatted(entityType.getSimpleName()));
            return synModelProvider.getModels(entityType)
                    .stream()
                    .map(QUERY_MODEL_TO_STAGE_1_TRANSFORMER::generateAsUncorrelatedSourceQuery)
                    .collect(toImmutableList());
        }
    }

    private static CalcPropInfo toCalcPropInfo(final PropertyMetadata.Calculated pm) {
        return new CalcPropInfo(pm.data().expressionModel(), pm.data().implicit(), pm.data().forTotals());
    }

}
