package ua.com.fielden.platform;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;

import java.util.List;

/// A utility that brings the DB schema in line with the indexes that the current DDL defines for union-typed properties.
///
/// For each union-typed persistent property, the DDL defines an index on the expression for its `id`, which lists the union members:
/// * For SQL Server, a computed column with that expression along with an index on it.
/// * For PostgreSQL, an expression index.
///
/// This utility generates SQL scripts for the following cases:
/// * Upgrading a database created before such indexes were introduced — [#createSql()].
/// * Changing the members of a union type, which changes the expression for every property of that type — [#dropSql()] and [#createSql()].
///
/// [#findOutdated()] compares the database with the current DDL and reports the indexes that are missing or outdated.
///
/// Only union-typed properties of the current domain are covered.
/// Computed columns and indexes for properties that are no longer union-typed, or no longer exist, are not reported or dropped.
///
@ImplementedBy(ComputedUnionColumnUpgradeImpl.class)
public interface IComputedUnionColumnUpgrade {

    /// Produces an SQL script that creates the missing computed union columns and indexes for the selected persistent union-typed properties.
    /// Existing computed columns and indexes are skipped, so the script can be rerun.
    ///
    /// Refer to [#dropSql()] for its use when the members of a union type change.
    ///
    UnionPropertySelector<String> createSql();

    /// Selects persistent union-typed properties for an operation, which produces a result of type `T`.
    ///
    interface UnionPropertySelector<T> {

        /// Selects all properties from all registered domain types.
        ///
        T allInDomain();

        /// Selects all properties that are present in the specified entity types.
        ///
        T propertiesOf(Iterable<Class<? extends AbstractEntity<?>>> entityTypes);

        /// Selects the specified property.
        /// It is an error if the property is not union-typed.
        ///
        T property(Class<? extends AbstractEntity<?>> entityType, CharSequence property);

        /// Selects all properties in the application domain whose type is `unionType`.
        ///
        T propertiesWithType(Class<? extends AbstractUnionEntity> unionType);
    }

    /// Produces an SQL script that drops the computed union columns and indexes for the selected persistent union-typed properties.
    /// Computed columns and indexes that do not exist are skipped, so the script can be rerun.
    ///
    /// When the members of a union type change, the expression for each of its properties changes, and existing indexes no longer match it.
    /// They are recreated for the properties selected by [UnionPropertySelector#propertiesWithType(Class)] for that union type:
    ///
    /// 1. Execute this script before adding or dropping member columns.
    ///    SQL Server does not allow dropping a column that is used by a computed column.
    /// 2. Add or drop the member columns.
    /// 3. Execute the script produced by [#createSql()].
    ///
    UnionPropertySelector<String> dropSql();

    /// @deprecated Use `createSql().allInDomain()`.
    ///
    @Deprecated(forRemoval = true, since = "2.5.12")
    default String sql() {
        return createSql().allInDomain();
    }

    /// @deprecated Use `createSql().propertiesOf(entityTypes)`.
    ///
    @Deprecated(forRemoval = true, since = "2.5.12")
    default String sql(final Iterable<Class<? extends AbstractEntity<?>>> entityTypes) {
        return createSql().propertiesOf(entityTypes);
    }

    /// @deprecated Use `createSql().property(entityType, property)`.
    ///
    @Deprecated(forRemoval = true, since = "2.5.12")
    default String sql(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
        return createSql().property(entityType, property);
    }

    /// @deprecated Use `createSql().propertiesWithType(unionType)`.
    ///
    @Deprecated(forRemoval = true, since = "2.5.12")
    default String createSql(final Class<? extends AbstractUnionEntity> unionType) {
        return createSql().propertiesWithType(unionType);
    }

    /// @deprecated Use `dropSql().propertiesWithType(unionType)`.
    ///
    @Deprecated(forRemoval = true, since = "2.5.12")
    default String dropSql(final Class<? extends AbstractUnionEntity> unionType) {
        return dropSql().propertiesWithType(unionType);
    }

    /// Compares the computed union columns and indexes in the database with those defined by the current DDL.
    /// Returns those that are missing or outdated, and an empty list if the database is up to date.
    ///
    /// * A [Problem#MISSING] index is created by [#createSql()].
    /// * An [Problem#OUTDATED] index is recreated by [#dropSql()] followed by [#createSql()] for the properties of its union type.
    ///
    List<OutdatedUnionIndex> findOutdated();

    /// A union-typed property whose computed column or index in the database differs from the current DDL.
    ///
    /// Its string representation is a single line for printing, e.g., `MeterReading.capable (MeterCapable): OUTDATED — <details>`.
    ///
    /// @param entityType  the entity type that declares the property
    /// @param property  the union-typed property
    /// @param unionType  the type of the property
    /// @param problem  the kind of difference
    /// @param details  a description of the difference
    ///
    record OutdatedUnionIndex(
            Class<? extends AbstractEntity<?>> entityType,
            String property,
            Class<? extends AbstractUnionEntity> unionType,
            Problem problem,
            String details)
    {
        @Override
        public String toString() {
            return "%s.%s (%s): %s — %s".formatted(entityType.getSimpleName(), property, unionType.getSimpleName(), problem, details);
        }
    }

    /// The kind of difference between the database and the current DDL.
    ///
    enum Problem {
        /// The computed column (SQL Server) or the index does not exist.
        MISSING,
        /// The computed column or the index exists, but differs from the current DDL (e.g., it lists different union members).
        OUTDATED
    }

}
