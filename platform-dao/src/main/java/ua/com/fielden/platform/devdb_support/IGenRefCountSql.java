package ua.com.fielden.platform.devdb_support;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Collection;
import java.util.List;

/// Generates SQL to update `refCount` for all persistent activatable entities.
///
/// Only references between activatable persistent entities are considered.
/// [DependencyPredicate] can be used to further filter references.
///
/// @author TG Team
///
@ImplementedBy(GenRefCountSqlImpl.class)
public interface IGenRefCountSql {

    /// Generates SQL statements that update `refCount` of entities in `entityTypes`.
    /// The `refCount` is calculated for each entity dependency that satisfies `dependencyPredicate`.
    ///
    List<String> generateStatements(Collection<Class<? extends AbstractEntity<?>>> entityTypes, DependencyPredicate dependencyPredicate);

    /// Similar to [#generateStatements(Collection, DependencyPredicate)], but the predicate is always true.
    ///
    List<String> generateStatements(Collection<Class<? extends AbstractEntity<?>>> entityTypes);

    /// Similar to [#generateStatements(Collection, DependencyPredicate)], but the whole application domain is used as a source of entity types.
    ///
    List<String> generateStatements(DependencyPredicate dependencyPredicate);

    /// Similar to [#generateStatements(Collection, DependencyPredicate)], but the whole application domain is used as a source of entity types,
    /// and the predicate is always true.
    ///
    List<String> generateStatements();

    @FunctionalInterface
    interface DependencyPredicate {

        /// Implements the predicate.
        ///
        /// `property` may be one of the following:
        /// 1. Simple property name. E.g., `equipment`.
        /// 2. Path to a union member. E.g., `asset.equipment`.
        ///
        /// @param entityType  type of the entity being updated
        /// @param dependencyType  type of the entity that references `entityType`
        /// @param property  path to a property whose type is `entityType`. This path starts in `dependencyType`.
        ///
        boolean test(Class<? extends AbstractEntity<?>> entityType, Class<? extends AbstractEntity<?>> dependencyType, CharSequence property);

        static DependencyPredicate constantly(final boolean value) {
            return ($1, $2, $3) -> value;
        }

    }

}
