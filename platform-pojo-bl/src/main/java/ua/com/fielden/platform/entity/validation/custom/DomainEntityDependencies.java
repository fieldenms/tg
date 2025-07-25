package ua.com.fielden.platform.entity.validation.custom;

import com.google.common.collect.ImmutableSet;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.reflection.Finder;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isNotSpecialActivatableToBeSkipped;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.splitPropPathToArray;

/**
 * Identifies activatable dependencies for an entity of the specified type.
 */
public class DomainEntityDependencies {
    public final Class<? extends AbstractEntity<?>> entityType;
    public final boolean activatable;
    private final Set<Class<? extends ActivatableAbstractEntity<?>>> automaticallyDeactivatedDependencies;
    private final Set<DomainEntityDependency> dependencies = new HashSet<>();

    public DomainEntityDependencies(final Class<? extends AbstractEntity<?>> entityType) {
        this.entityType = entityType;
        this.activatable = isActivatableEntityType(entityType);

        if (activatable && entityType.isAnnotationPresent(DeactivatableDependencies.class)) {
            final DeactivatableDependencies annotation = ((Class<? extends ActivatableAbstractEntity<?>>) entityType).getAnnotation(DeactivatableDependencies.class);
            automaticallyDeactivatedDependencies = ImmutableSet.copyOf(annotation.value());
        }
        else {
            automaticallyDeactivatedDependencies = ImmutableSet.of();
        }
    }

    /**
     * Returns a subset of activatable dependencies, ignoring deactivatable dependencies.
     */
    public Set<DomainEntityDependency> getActivatableDependencies() {
        return dependencies.stream()
               .filter(dep -> dep.shouldBeCheckedDuringDeactivation && !(automaticallyDeactivatedDependencies.contains(dep.entityType) && dep.belongsToEntityKey))
               .collect(toSet());
    }

    /**
     * Returns a subset of dependencies stemming from entities specified in {@link DeactivatableDependencies},
     * declared for the entity type of this {@link DomainEntityDependencies} instance.
     */
    private Stream<DomainEntityDependency> getImmediateDeactivatableDependencies() {
        return dependencies.stream()
                .filter(dep -> dep.shouldBeCheckedDuringDeactivation && automaticallyDeactivatedDependencies.contains(dep.entityType));
    }

    /**
     * Returns a subset of all deactivatable dependencies, including those stemming from immediate and transitive declarations of @link DeactivatableDependencies}.
     */
    public Stream<DomainEntityDependency> getAllDeactivatableDependencies(final Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> domainDependencies) {
        return getAllDeactivatableDependencies(getImmediateDeactivatableDependencies(), domainDependencies);
    }

    /**
     * Recursively traverses deactivatable dependencies, gathering their direct and deactivatable dependencies.
     */
    private static Stream<DomainEntityDependency> getAllDeactivatableDependencies(
            final Stream<DomainEntityDependency> sameLevelDeactivatableDependencies,
            final Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> domainDependencies)
    {
        return sameLevelDeactivatableDependencies.flatMap(dep -> {
            final var domainEntityDependencies = domainDependencies.get(dep.entityType);
            final var directDependencies = domainEntityDependencies.getActivatableDependencies().stream().map(d -> d.updatePropPath(dep.propPath));
            final var transitiveDependencies = domainEntityDependencies.getImmediateDeactivatableDependencies().map(d -> d.updatePropPath(dep.propPath));
            return Stream.concat(directDependencies, getAllDeactivatableDependencies(transitiveDependencies, domainDependencies));
        });
    }

    /**
     * Returns all dependencies (direct and all transitive stemming from deactivatable dependencies) that might prevent entity deactivation.
     *
     * @param domainDependencies  Domain dependencies, which should be considered, usually containing all activatable and persistent domain entities.
     */
    public Stream<DomainEntityDependency> getAllDependenciesThatCanPreventDeactivation(
            final Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> domainDependencies)
    {
        // Direct dependencies.
        final var directActivatableDependenciesForEntity = getActivatableDependencies();
        // Deactivatable dependencies, which are calculated by recursively traversing all deactivatable dependencies for the current entity,
        // all deactivatable dependencies for them, and so on.
        final var deactivatableActivatableDependenciesForEntity = getAllDeactivatableDependencies(domainDependencies);
        // Merge direct and deactivatable dependencies for processing.
        return Stream.concat(directActivatableDependenciesForEntity.stream(), deactivatableActivatableDependenciesForEntity);
    }

    public void addDependency(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propPath) {
        dependencies.add(new DomainEntityDependency(entityType, propPath.toString()));
    }

    /**
     * A convenient struct to represent a single property as a dependency.
     */
    public static class DomainEntityDependency {
        public final Class<? extends AbstractEntity<?>> entityType;
        public final String entityTitle;
        public final String propPath;
        public final String propTitle;
        public final boolean shouldBeCheckedDuringDeactivation;
        public final boolean belongsToEntityKey;

        private DomainEntityDependency(
                Class<? extends AbstractEntity<?>> entityType,
                String entityTitle,
                String propPath,
                String propTitle,
                boolean shouldBeCheckedDuringDeactivation,
                boolean belongsToEntityKey)
        {
            this.entityType = entityType;
            this.entityTitle = entityTitle;
            this.propPath = propPath;
            this.propTitle = propTitle;
            this.shouldBeCheckedDuringDeactivation = shouldBeCheckedDuringDeactivation;
            this.belongsToEntityKey = belongsToEntityKey;
        }

        public Class<? extends AbstractEntity<?>> entityType() {
            return entityType;
        }

        public String propPath() {
            return propPath;
        }

        private DomainEntityDependency(final Class<? extends AbstractEntity<?>> entityType, final String propPath) {
            this(entityType,
                 getEntityTitleAndDesc(entityType).getKey(),
                 propPath,
                 getTitleAndDesc(propPath, entityType).getKey(),
                 checkDuringDeactivation(entityType, propPath),
                 getKeyMembers(entityType).stream().map(Field::getName).anyMatch(propPath::equals));
        }

        public DomainEntityDependency updatePropPath(final CharSequence propPathSuffix) {
            return  new DomainEntityDependency(entityType, entityTitle, propPath + "." + propPathSuffix, propTitle, belongsToEntityKey, shouldBeCheckedDuringDeactivation);
        }

        private static boolean checkDuringDeactivation(final Class<? extends AbstractEntity<?>> entityType, final String propPath) {
            final Field prop0 = Finder.getFieldByName(entityType, splitPropPathToArray(propPath)[0]);
            final var seevAnnotation = prop0.getAnnotation(SkipEntityExistsValidation.class);
            final boolean skipActiveOnly = seevAnnotation != null && seevAnnotation.skipActiveOnly();
            return isActivatableEntityType(entityType) && isNotSpecialActivatableToBeSkipped(prop0) && !skipActiveOnly;
        }

        public static final String INFO_ENTITY_DEPENDENCIES = "Entity [%s] has dependency in entity [%s] as property [%s] (checked during deactivation [%s], belongs to entity key [%s]).";
        @Override
        public String toString() {
            return INFO_ENTITY_DEPENDENCIES.formatted(entityType.getName(), entityType.getName(), propPath, shouldBeCheckedDuringDeactivation, belongsToEntityKey);
        }

    }
}
