package ua.com.fielden.platform.eql.meta;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.query.*;
import ua.com.fielden.platform.eql.meta.utils.DependentCalcPropsOrder;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnQueries;
import ua.com.fielden.platform.eql.stage2.queries.SourceQuery2;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyTypeMetadata;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.eql.meta.utils.TopologicalSort.sortTopologically;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.types.tuples.T2.t2;

@Singleton
public class QuerySourceInfoProvider {
    private static final Logger LOGGER = getLogger(QuerySourceInfoProvider.class);

    /**
     * Declared - all properties (as defined by EqlEntityMetadata) are included (even those with no way of getting data from a db for them).
     */
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> declaredQuerySourceInfoMap;

    /**
     * Modelled - only those properties are included that are either mapped to a table/query column or are calculated.
     */
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> modelledQuerySourceInfoMap;

    /** Models for Synthetic Entities (SE), transformed to stage 1. */
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, List<SourceQuery1>> seModels;

    private final ConcurrentMap<String, List<String>> entityTypesDependentCalcPropsOrder = new ConcurrentHashMap<>();
    private final IDomainMetadata domainMetadata;

    @Inject
    public QuerySourceInfoProvider(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
        final var qmToS1Transformer = new QueryModelToStage1Transformer();

        declaredQuerySourceInfoMap = domainMetadata.allTypes(EntityMetadata.class)
                .collect(toConcurrentMap(EntityMetadata::javaType, em -> new QuerySourceInfo<>(em.javaType(), true)));
        declaredQuerySourceInfoMap.values()
                .forEach(ei -> ei.addProps(generateQuerySourceItems(declaredQuerySourceInfoMap, ei.javaType())));

        modelledQuerySourceInfoMap = domainMetadata.allTypes(EntityMetadata.class)
                .filter(em -> em.isPersistent() || em.isUnion())
                .collect(toConcurrentMap(EntityMetadata::javaType, em -> new QuerySourceInfo<>(em.javaType(), true)));
        modelledQuerySourceInfoMap.values()
                .forEach(ei -> ei.addProps(generateQuerySourceItems(modelledQuerySourceInfoMap, ei.javaType())));

        // Generating models and dependency information for SE types
        // There is no need to include UE types here as their models are implicitly generated and have no interdependencies.
        seModels = new ConcurrentHashMap<>();
        final var seDependencies = new HashMap<Class<? extends AbstractEntity<?>>, Set<Class<? extends AbstractEntity<?>>>>();
        domainMetadata.allTypes(EntityMetadata.class)
                .map(EntityMetadata::asSynthetic).flatMap(Optional::stream)
                .forEach(em -> {
                    final T2<List<SourceQuery1>, Set<Class<? extends AbstractEntity<?>>>> res = generateModelsAndDependenciesForSyntheticType(em, qmToS1Transformer);
                    seModels.put(em.javaType(), res._1);
                    seDependencies.put(em.javaType(), res._2.stream().filter(EntityUtils::isSyntheticEntityType).collect(toSet()));
                });

        for (final Class<? extends AbstractEntity<?>> seType : sortTopologically(seDependencies)) {
            try {
                final QuerySourceInfo<? extends AbstractEntity<?>> modelledQuerySourceInfo = generateModelledQuerySourceInfoForSyntheticType(seType, seModels.get(seType));
                modelledQuerySourceInfoMap.put(modelledQuerySourceInfo.javaType(), modelledQuerySourceInfo);
            } catch (final Exception e) {
                final var msg = "Could not generate modelled entity info for synthetic entity [" + seType + "].";
                LOGGER.error(msg, e);
                throw new EqlMetadataGenerationException(msg);
            }
        }

        for (final QuerySourceInfo<?> querySourceInfo : modelledQuerySourceInfoMap.values()) {
            entityTypesDependentCalcPropsOrder.put(querySourceInfo.javaType().getName(), DependentCalcPropsOrder.orderDependentCalcProps(this, qmToS1Transformer, querySourceInfo));
        }
    }

    private static <T extends AbstractEntity<?>> T2<List<SourceQuery1>, Set<Class<? extends AbstractEntity<?>>>>
    generateModelsAndDependenciesForSyntheticType(final EntityMetadata.Synthetic em, final QueryModelToStage1Transformer gen)
    {
        final var queries = em.data().models().stream().map(gen::generateAsUncorrelatedSourceQuery).toList();
        final var dependencies = queries.stream().map(qry -> qry.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
        return t2(queries, dependencies);
    }

    /**
     * Only properties that are present in SE yields are preserved.
     *
     * @return
     */
    private <T extends AbstractEntity<?>> QuerySourceInfo<?> generateModelledQuerySourceInfoForSyntheticType(final Class<? extends AbstractEntity<?>> entityType, final List<SourceQuery1> queries) {
        final TransformationContextFromStage1To2 context = TransformationContextFromStage1To2.forMainContext(this);
        final List<SourceQuery2> transformedQueries = queries.stream().map(m -> m.transform(context)).collect(toList());
        return Source1BasedOnQueries.produceQuerySourceInfoForEntityType(this, transformedQueries, entityType, true /*isComprehensive*/);
    }

    public List<String> getCalcPropsOrder(final Class<? extends AbstractEntity<?>> entityType) {
        // TODO: It is assumed that there would be no generated types with newly added dependent calc props.
        //       This assumption needs to be revisited when implementing support for user-definable calculated properties.
        return entityTypesDependentCalcPropsOrder.get(getOriginalType(entityType).getName());
    }

    private List<AbstractQuerySourceItem<?>> generateQuerySourceItems(
            final Map<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> allQuerySourceInfos,
            final Class<? extends AbstractEntity<?>> entityType)
    {
        final var pmUtils = domainMetadata.propertyMetadataUtils();
        final var entityMetadata = domainMetadata.forEntity(entityType);

        // Exclude properties that are irrelevant to EQL.
        // TODO: Formally define the set of EQL-relevant properties and define a corresponding predicate.
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

    /**
     * @param type  entity type which must be reifiable with metadata
     */
    public QuerySourceInfo<?> getDeclaredQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        final QuerySourceInfo<?> existing = declaredQuerySourceInfoMap.get(type);
        return existing != null ? existing : generateDeclaredQuerySourceInfo(type);
    }

    /**
     * @param type  entity type which must be reifiable with metadata
     */
    public QuerySourceInfo<?> getModelledQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        final QuerySourceInfo<?> existing = modelledQuerySourceInfoMap.get(type);
        if (existing != null) {
            return existing;
        }

        if (domainMetadata.forEntity(type) instanceof EntityMetadata.Synthetic) {
            return generateModelledQuerySourceInfoForSyntheticType(type, seModels.get(getOriginalType(type)));
        }
        return generateModelledQuerySourceInfoForPersistentType(type);
    }

    private static CalcPropInfo toCalcPropInfo(final PropertyMetadata.Calculated pm) {
        return new CalcPropInfo(pm.data().expressionModel(), pm.data().implicit(), pm.data().forTotals());
    }

}
