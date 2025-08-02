package ua.com.fielden.platform.entity.validation.custom;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.validation.custom.DomainEntityDependencies.DomainEntityDependency;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

/**
 * A set of utilities for analysing domain dependencies.
 */
public class DomainEntitiesDependenciesUtils {
    public static final String PARAM = "ENTITY_VALUE";
    public static final String ENTITY_TYPE_NAME = "entity_type_name";
    public static final String ENTITY_TYPE_TITLE = "entity_type_title";
    public static final String DEPENDENT_PROP_PATH = "dependent_prop_path";
    public static final String DEPENDENT_PROP_TITLE = "dependent_prop_title";
    public static final String COUNT = "KOUNT";

    /**
     * A factory method to generate a query for retrieving entity dependencies on either all (`deactivationOnly = false`)
     * or active only (`deactivationOnly = true`) entity instances of types specified in `dependencies`.
     *
     * @param dependencies
     *           A set of dependencies, which is determined elsewhere.
     * @param deactivationOnly
     *           A parameter controlling whether only active dependencies should be counted.
     * @return
     *           An aggregate query model to retrieve the total count of entity dependencies per dependency entity type and property.
     *           The shape of the query result is `(entity title, property title, count)`.
     */
    public static AggregatedResultQueryModel dependencyCountQuery(final Set<DomainEntityDependency> dependencies, final boolean deactivationOnly) {
        final var models = dependencies.stream().map(dependency -> {
            final var cond = deactivationOnly
                             ? cond().prop(ID).ne().param(PARAM).and().prop(dependency.propPath()).eq().param(PARAM).and().prop(ACTIVE).eq().val(true).model()
                             : cond().prop(ID).ne().param(PARAM).and().prop(dependency.propPath()).eq().param(PARAM).model();
            return select(dependency.entityType())
                   .where().condition(cond)
                   .yield().val(dependency.entityType().getName()).as(ENTITY_TYPE_NAME)
                   .yield().val(dependency.entityTitle()).as(ENTITY_TYPE_TITLE)
                   .yield().val(dependency.propPath()).as(DEPENDENT_PROP_PATH)
                   .yield().val(dependency.propTitle()).as(DEPENDENT_PROP_TITLE)
                  .modelAsAggregate();
        }).toArray(AggregatedResultQueryModel[]::new);

        return select(models)
               .groupBy().prop(ENTITY_TYPE_NAME)
               .groupBy().prop(DEPENDENT_PROP_PATH)
               .groupBy().prop(ENTITY_TYPE_TITLE)
               .groupBy().prop(DEPENDENT_PROP_TITLE)
               .yield().prop(ENTITY_TYPE_TITLE).as(ENTITY_TYPE_TITLE)
               .yield().prop(DEPENDENT_PROP_TITLE).as(DEPENDENT_PROP_TITLE)
               .yield().countAll().as(COUNT)
               .modelAsAggregate();
    }

    /**
     * Method {@link #entityDependencyMap(Collection)} or {@link #entityDependencyMap(Collection, Predicate)} should be used instead.
     */
    @Deprecated(forRemoval = true, since = "1.7.0")
    public static Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> getEntityDependantsMap(final Collection<Class<? extends AbstractEntity<?>>> domainEntityTypes) {
        return entityDependencyMap(domainEntityTypes);
    }

    /**
     * A function that builds a dependency map for each persistent entity type.
     * As a result, each entity type (call it a target entity) gets associated with an instance of [DomainEntityDependencies],
     * which contains a set of [DomainEntityDependency], representing pairs of entity types and their properties of the target entity type.
     *
     * @param domainEntityTypes
     *          A collection of domain entities to be analysed; usually a complete domain is expected;
     * @param entityTypePredicate
     *          An inclusive predicate to identify types that should be considered for dependency analysis.
     * @return  A map between persistent entity types and their persistent dependencies.
     */
    @SuppressWarnings("unchecked")
    public static Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> entityDependencyMap(
            final Collection<Class<? extends AbstractEntity<?>>> domainEntityTypes,
            final Predicate<Class<? extends AbstractEntity<?>>> entityTypePredicate)
    {
        final var map = new HashMap<Class<? extends AbstractEntity<?>>, DomainEntityDependencies>();
        domainEntityTypes.stream().filter(entityTypePredicate)
        .forEach(entType -> {
            // Need to make sure that every matching entity type has a corresponding instance of DomainEntityDependencies.
            // Otherwise, there can be situations where an entity that has no properties of its type, ends up not represented in the dependency map.
            map.computeIfAbsent(entType, DomainEntityDependencies::new);

            // Process real properties.
            // This processing works inversely, where properties in the entity being processed have the dependency map
            streamRealProperties(entType, MapTo.class).forEach(field -> {
                if (isUnionEntityType(field.getType())) {
                    for (final var unionMember : unionProperties((Class<? extends AbstractUnionEntity>) field.getType())) {
                        final var unionMemberType = (Class<? extends AbstractEntity<?>>) unionMember.getType();
                        if (entityTypePredicate.test(unionMemberType)) {
                            map.computeIfAbsent(unionMemberType, DomainEntityDependencies::new)
                                    .addDependency(entType, "%s.%s".formatted(field.getName(), unionMember.getName()));
                        }
                    }
                }
                else if (entityTypePredicate.test((Class<? extends AbstractEntity<?>>) field.getType())) {
                    map.computeIfAbsent((Class<? extends AbstractEntity<?>>) field.getType(), DomainEntityDependencies::new)
                            .addDependency(entType, field.getName());
                }
            });

            // Process a special case of one-2-one association.
            if (isOneToOne(entType)) {
                final var keyField = getKeyMembers(entType).getFirst();
                map.get(entType)
                   .addDependency(entType, keyField.getName());
            }
        });

        return map;
    }

    /**
     * Equivalent to {@link #entityDependencyMap(Collection, Predicate)}, where {@code predicate} simply tests that entity is persistent.
     */
    public static Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> entityDependencyMap(final Collection<Class<? extends AbstractEntity<?>>> domainEntityTypes) {
        return entityDependencyMap(domainEntityTypes, EntityUtils::isPersistentEntityType);
    }

}
