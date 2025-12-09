package ua.com.fielden.platform.entity.validation.custom;

import com.google.common.collect.ImmutableSet;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.reflection.Finder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isActivatablePersistentProperty;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isSpecialActivatableToBeSkipped;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.isKeyOrKeyMember;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/// Represents an entity type and its dependencies.
///
/// For an entity type A, its dependency D is a persistent property typed with A and declared in entity type B.
/// A and B may be the same type.
///
public class DomainEntityDependencies {

    public static final String
            ERR_UNEXPECTED_DEPENDENCY_PATH = "Unexpected dependency representation: entityType = [%s], propPath = [%s].",
            INFO_ENTITY_DEPENDENCIES = "Entity [%s] has dependency in entity [%s] as property [%s] (checked during deactivation [%s], belongs to entity key [%s]).";

    private final Class<? extends AbstractEntity<?>> entityType;
    private final Set<Class<? extends ActivatableAbstractEntity<?>>> deactivatableDependencies;
    private final Set<DomainEntityDependency> dependencies = new HashSet<>();

    DomainEntityDependencies(final Class<? extends AbstractEntity<?>> entityType) {
        this.entityType = entityType;

        final DeactivatableDependencies annot;
        if (isActivatablePersistentEntityType(entityType) && (annot = getAnnotation(entityType, DeactivatableDependencies.class)) != null) {
            deactivatableDependencies = ImmutableSet.copyOf(annot.value());
        }
        else {
            deactivatableDependencies = ImmutableSet.of();
        }
    }

    public Class<? extends AbstractEntity<?>> entityType() {
        return entityType;
    }

    /// Returns a subset of activatable dependencies, ignoring deactivatable dependencies.
    ///
    public Set<DomainEntityDependency> getActivatableDependencies() {
        return dependencies.stream()
               .filter(dep -> dep.shouldBeCheckedDuringDeactivation && !(deactivatableDependencies.contains(dep.entityType) && dep.belongsToEntityKey))
               .collect(toSet());
    }

    /// Returns a subset of dependencies stemming from entities specified in [DeactivatableDependencies],
    /// declared for the entity type of this [DomainEntityDependencies] instance.
    ///
    private Stream<DomainEntityDependency> getImmediateDeactivatableDependencies() {
        return dependencies.stream()
                .filter(dep -> dep.shouldBeCheckedDuringDeactivation && deactivatableDependencies.contains(dep.entityType));
    }

    /// Returns a subset of all deactivatable dependencies, including those stemming from immediate and transitive declarations of [DeactivatableDependencies].
    ///
    public Stream<DomainEntityDependency> getAllDeactivatableDependencies(final Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> domainDependencies) {
        return getAllDeactivatableDependencies(getImmediateDeactivatableDependencies(), domainDependencies);
    }

    /// Recursively traverses deactivatable dependencies, gathering their direct and deactivatable dependencies.
    ///
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

    /// Returns all dependencies (direct and all transitive stemming from deactivatable dependencies) that might prevent entity deactivation.
    ///
    /// @param domainDependencies  Domain dependencies, which should be considered, usually containing all activatable and persistent domain entities.
    ///
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

    /// A convenient struct to represent a single property as a dependency.
    ///
    /// @param entityType  type of the property
    ///
    public record DomainEntityDependency(
            Class<? extends AbstractEntity<?>> entityType,
            String entityTitle,
            String propPath,
            String propTitle,
            boolean shouldBeCheckedDuringDeactivation,
            boolean belongsToEntityKey)
    {

        private DomainEntityDependency(final Class<? extends AbstractEntity<?>> entityType, final String propPath) {
            this(entityType,
                 getEntityTitleAndDesc(entityType).getKey(),
                 propPath,
                 getTitleAndDesc(propPath, entityType).getKey(),
                 checkDuringDeactivation(entityType, propPath),
                 belongsToEntityKey(entityType, propPath));
        }

        public DomainEntityDependency updatePropPath(final CharSequence propPathSuffix) {
            return new DomainEntityDependency(entityType, entityTitle, propPath + "." + propPathSuffix, propTitle, belongsToEntityKey, shouldBeCheckedDuringDeactivation);
        }

        /// Determines if `propPath` represents a property that should be checked during the analysis of active dependencies.
        ///
        /// The value of `propPath` is a dot-expression only when it represents a union-typed property with a specific union member property.
        /// The case of union entities follows the two-level approach, consistent with how it is for [EntityExistsValidator]:
        ///
        /// 1. If for union-typed property (top level) check is `false`, then return the check result for the union member property (lower level).
        /// 2. If for union-typed property (top level) check is `true`, then return `true` (there is no need to check the union member property (lower level)).
        ///
        private static boolean checkDuringDeactivation(final Class<? extends AbstractEntity<?>> entityType, final String propPath) {
            final String[] props = splitPropPathToArray(propPath);
            final var prop0Name = props[0];
            final var prop0 = Finder.getFieldByName(entityType, prop0Name);

            // If entityType is not persistent activatable, then no need to check anything else -- activatable nature is not applicable.
            if (!isActivatablePersistentEntityType(entityType)) {
                return false;
            }

            // Otherwise, need to check the property itself, and perhaps even a union member property in case of a union entity.
            final var checkDuringDeactivationProp0 = !isSpecialActivatableToBeSkipped(prop0)
                                                     && isActivatablePersistentProperty(entityType, prop0Name);
            if (checkDuringDeactivationProp0) {
                return true;
            }
            final Class<?> prop0Type = prop0.getType();
            if (isUnionEntityType(prop0Type) && props.length == 2) {
                final var prop1Name = props[1];
                final var unionMemberProp = Finder.getFieldByName(prop0Type, prop1Name);
                final var checkDuringDeactivationProp1 = !isSpecialActivatableToBeSkipped(unionMemberProp)
                                                         && isActivatablePersistentProperty((Class<? extends AbstractEntity<?>>) prop0Type, prop1Name);
                return checkDuringDeactivationProp1;
            }

            return false;
        }

        /// Identifies whether `propPath` can be considered a key member of `entityType`.
        /// Handles the case of union-typed key members, where `propPath` represents a union member (dot-expression).
        ///
        private static boolean belongsToEntityKey(final Class<? extends AbstractEntity<?>> entityType, final String propPath) {
            final String[] props = splitPropPathToArray(propPath);
            final var prop0 = Finder.getFieldByName(entityType, props[0]);
            // If `propPath` is just a property name rather than a dot-expression, we delegate the call to [Finder#isKeyOrKeyMember].
            if (props.length == 1) {
                return isKeyOrKeyMember(prop0);
            }
            // If `propPath` a dot-expression with 2 properties that represent a union member,
            // we check if the first property in the path (i.e. the union-typed property) represents a key.
            // Here we assume that the second property in the path is a union member.
            else if (props.length == 2 && isUnionEntityType(prop0.getType())) {
                return isKeyOrKeyMember(prop0);
            }
            // Otherwise, we have an invalid situation, which cannot be processed any further.
            else {
                throw new InvalidArgumentException(ERR_UNEXPECTED_DEPENDENCY_PATH.formatted(entityType.getTypeName(), propPath));
            }
        }

        @Override
        public String toString() {
            return INFO_ENTITY_DEPENDENCIES.formatted(entityType.getName(), entityType.getName(), propPath, shouldBeCheckedDuringDeactivation, belongsToEntityKey);
        }
    }

}
