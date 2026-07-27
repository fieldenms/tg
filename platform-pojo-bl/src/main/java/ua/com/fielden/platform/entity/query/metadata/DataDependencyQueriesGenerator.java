package ua.com.fielden.platform.entity.query.metadata;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.data.generator.WithCreatedByUser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

/**
 * Provides static methods for generating EQL queries, which are used for inspecting references between entity instances.
 *
 * @author TG Team
 *
 */
public class DataDependencyQueriesGenerator {

    public static Optional<QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel>> queryForDependentTypesSummary(
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata,
            final Long entityId,
            final Class<? extends AbstractEntity<?>> entityType)
    {
        return queryForDependentTypesSummary(dependenciesMetadata, entityId, entityType, false);
    }

    public static Optional<QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel>> queryForDependentTypesSummary(
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata,
            final Long entityId,
            final Class<? extends AbstractEntity<?>> entityType,
            final boolean activeOnly)
    {
        final AggregatedResultQueryModel[] queries = produceQueries(dependenciesMetadata, entityType, entityId, activeOnly).toArray(new AggregatedResultQueryModel[] {});
        // if no property references to the specified entity type exists, then there is nothing to query
        if (queries.length == 0) {
            return empty();
        }

        final AggregatedResultQueryModel qry = select(queries)
                                               .groupBy().prop("type")
                                               .groupBy().prop("simpleType")
                                               .yield().prop("type").as("type")
                                               .yield().prop("simpleType").as("simpleType")
                                               .yield().countAll().as("qty").modelAsAggregate();
        return Optional.of(from(qry).with(orderBy().yield("qty").desc().yield("simpleType").asc().model()).model());
    }

    public static QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> queryForDependentTypeDetails(
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata,
            final Long entityId,
            final Class<? extends AbstractEntity<?>> entityType,
            final Class<? extends AbstractEntity<?>> detailsType,
            final fetch<? extends AbstractEntity<?>> fetchModel)
    {
        return queryForDependentTypeDetails(dependenciesMetadata, entityId, entityType, detailsType, fetchModel, false);
    }

    public static QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> queryForDependentTypeDetails(
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata,
            final Long entityId,
            final Class<? extends AbstractEntity<?>> entityType,
            final Class<? extends AbstractEntity<?>> detailsType,
            final fetch<? extends AbstractEntity<?>> fetchModel,
            final boolean activeOnly)
    {
        final PrimitiveResultQueryModel[] detailsQueries = produceDetailsQueries(dependenciesMetadata, detailsType, activeOnly).toArray(new PrimitiveResultQueryModel[] {});
        final ExpressionModel hasDependencies = detailsQueries.length > 0
                ? expr().caseWhen().existsAnyOf(detailsQueries).then().val("Y").otherwise().val("N").end().model()
                : expr().val("N").model();
        final var partialQ = select(detailsType).where().anyOfProps(dependenciesMetadata.get(detailsType).get(entityType).toArray(new String[] {})).eq().val(entityId);
        final var qry = enhanceQueryWithActiveCondition(detailsType, partialQ, activeOnly).
                        yield().model(select(detailsType).where().prop(ID).eq().extProp(ID).model()).as("entity").
                        yield().expr(hasDependencies).as("hasDependencies").
                        modelAsAggregate();
        qry.setFilterable(true);
        return from(qry).with(fetchAggregates().with("hasDependencies").with("entity", fetchModel)).with(orderBy().prop("key").asc().model()).model();
    }

    /**
     * Generates a map between persistent entity types and persistent entity types referenced by their properties.
     * This relationship is represented as a map between types and a set of property names to cater to situations where an entity type has several props of the same type.
     *
     * @param entityTypes  entity types for which mapping would be performed.
     */
    @SuppressWarnings("unchecked")
    public static Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> produceDependenciesMetadata(final List<Class<? extends AbstractEntity<?>>> entityTypes) {
        final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> result = new HashMap<>();
        for (final Class<? extends AbstractEntity<?>> entityType : entityTypes) {
            if (entityType.isAnnotationPresent(MapEntityTo.class) && AbstractPersistentEntity.class.isAssignableFrom(entityType) && !WithCreatedByUser.class.isAssignableFrom(entityType)) {

                final Map<Class<? extends AbstractEntity<?>>, Set<String>> pmd = new HashMap<>();

                for (final Field ep : findRealProperties(entityType)) {
                    if (ep.isAnnotationPresent(MapTo.class) && !KEY.equals(ep.getName()) && (AbstractPersistentEntity.class.isAssignableFrom(ep.getType()) || AbstractUnionEntity.class.isAssignableFrom(ep.getType()))) {
                        final boolean isUnionEntityProp = AbstractUnionEntity.class.isAssignableFrom(ep.getType());
                        final List<Field> props = isUnionEntityProp ? unionProperties((Class<? extends AbstractUnionEntity>) ep.getType()) : List.of(ep);
                        for (final Field subProp : props) {
                            Set<String> existing = pmd.get(subProp.getType());
                            if (existing == null) {
                                existing = new HashSet<String>();
                                pmd.put((Class<? extends AbstractEntity<?>>) subProp.getType(), existing);
                            }
                            existing.add(isUnionEntityProp ? ep.getName() + "." + subProp.getName() : subProp.getName());
                        }
                    }
                }

                if (!pmd.isEmpty()) {
                    result.put(entityType, pmd);
                }
            }
        }
        return result;
    }

    private static List<AggregatedResultQueryModel> produceQueries(
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata,
            final Class<? extends AbstractEntity<?>> entityType,
            final Long entityId,
            final boolean activeOnly)
    {
        return dependenciesMetadata.entrySet().stream()
               .filter(el -> el.getValue().containsKey(entityType))
               .map(el -> enhanceQueryWithActiveCondition(el.getKey(),
                                                 select(el.getKey()).where().anyOfProps(el.getValue().get(entityType).toArray(new String[] {})).eq().val(entityId), activeOnly)
                                                 .yield().val(el.getKey().getName()).as("type")
                                                 .yield().val(el.getKey().getSimpleName()).as("simpleType")
                                                 .yield().prop(ID).as("entity").modelAsAggregate())
               .toList();
    }

    private static List<PrimitiveResultQueryModel> produceDetailsQueries(
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata,
            final Class<? extends AbstractEntity<?>> entityType,
            boolean activeOnly)
    {
        return dependenciesMetadata.entrySet().stream()
               .filter(el -> el.getValue().containsKey(entityType))
               .map(el -> enhanceQueryWithActiveCondition(el.getKey(), select(el.getKey()).where().anyOfProps(el.getValue().get(entityType).toArray(new String[] {})).eq().extProp(ID), activeOnly).yield().prop(ID).modelAsPrimitive())
               .toList();
    }

    private static ICompoundCondition0<? extends AbstractEntity<?>> enhanceQueryWithActiveCondition(
            final Class<? extends AbstractEntity<?>> detailType,
            final ICompoundCondition0<? extends AbstractEntity<?>> queryToEnhance,
            final boolean activeOnly) {
        // The case for an activatable entity.
        if (isActivatableEntityType(detailType)) {
            return activeOnly
                   ? queryToEnhance.and().prop(ACTIVE).eq().val(true)
                   : queryToEnhance;

        }
        // The case for a non-activatable entity, where all such entities should be filtered out if activeOnly is true.
        else {
            return activeOnly
                   ? queryToEnhance.and().val(false).eq().val(true) // filter-out condition
                   : queryToEnhance;
        }
    }
}