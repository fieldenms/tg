package ua.com.fielden.platform.entity.query.metadata;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.data.generator.WithCreatedByUser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

/**
 * Provides static methods for generating EQL queries, which are used for inspecting references between entity instances.
 *
 * @author TG Team
 *
 */
public class DataDependencyQueriesGenerator {

    public static Optional<QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel>> queryForDependentTypesSummary(final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata, final Long entityId, final Class<? extends AbstractEntity<?>> entityType) {
        final AggregatedResultQueryModel[] queries = produceQueries(dependenciesMetadata, entityType, entityId).toArray(new AggregatedResultQueryModel[] {});
        // if no property references to the specified entity type exists, then there is nothing to query
        if (queries.length == 0) {
            return empty();
        }

        final AggregatedResultQueryModel qry = select(queries).groupBy().prop("type").yield().prop("type").as("type").yield().countAll().as("qty").modelAsAggregate();
        return Optional.of(from(qry).with(orderBy().yield("qty").desc().model()).model());
    }

    public static QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> queryForDependentTypeDetails(final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata, final Long entityId, final Class<? extends AbstractEntity<?>> entityType, final Class<? extends AbstractEntity<?>> detailsType, final fetch<? extends AbstractEntity<?>> fetchModel) {
        final PrimitiveResultQueryModel[] detailsQueries = produceDetailsQueries(dependenciesMetadata, detailsType).toArray(new PrimitiveResultQueryModel[] {});
        final ExpressionModel hasDependencies = detailsQueries.length > 0 ? expr().caseWhen().existsAnyOf(detailsQueries).then().val("Y").otherwise().val("N").end().model()
                : expr().val("N").model();
        final AggregatedResultQueryModel qry = select(detailsType).
                where().
                anyOfProps(dependenciesMetadata.get(detailsType).get(entityType).toArray(new String[] {})).eq().val(entityId).
                yield().model(select(detailsType).where().prop(ID).eq().extProp(ID).model()).as("entity").
                yield().expr(hasDependencies).as("hasDependencies").
                modelAsAggregate();
        qry.setFilterable(true);
        return from(qry).with(fetchAggregates().with("hasDependencies").with("entity", fetchModel)).with(orderBy().prop("key").asc().model()).model();
    }

    /**
     * Generates map between persistent entity types and persistent entity types referenced by its properties (represented as map between types and set of prop names (as type can contain several props of the same type)).
     *
     * @param entityTypes
     * @return
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
                        final List<Field> props = isUnionEntityProp ? unionProperties((Class<? extends AbstractUnionEntity>) ep.getType()) : asList(ep);
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

    private static List<AggregatedResultQueryModel> produceQueries(final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata, final Class<? extends AbstractEntity<?>> entityType, final Long entityId) {
        return dependenciesMetadata.entrySet().stream()
        .filter(el -> el.getValue().containsKey(entityType))
        .map(el -> select(el.getKey()).where().anyOfProps(el.getValue().get(entityType).toArray(new String[] {})).eq().val(entityId).yield().val(el.getKey().getName()).as("type").yield().prop(ID).as("entity").modelAsAggregate())
        .collect(toList());
    }

    private static List<PrimitiveResultQueryModel> produceDetailsQueries(final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata, final Class<? extends AbstractEntity<?>> entityType) {
        return dependenciesMetadata.entrySet().stream()
        .filter(el -> el.getValue().containsKey(entityType))
        .map(el -> select(el.getKey()).where().anyOfProps(el.getValue().get(entityType).toArray(new String[] {})).eq().extProp(ID).yield().prop(ID).modelAsPrimitive())
        .collect(toList());
    }
}