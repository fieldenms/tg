package ua.com.fielden.platform;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;

/// A utility that automates the upgrade to computed union columns.
///
/// Upgrading requires modifying the DB schema.
/// * For SQL Server, a computed column along with an index is created for each union-typed property.
/// * For PostgreSQL, an expression index is created for each union-typed property.
///
/// This utility generates SQL statements to modify the DB schema for the whole domain.
///
@ImplementedBy(ComputedUnionColumnUpgradeImpl.class)
public interface IComputedUnionColumnUpgrade {

    /// Produces an SQL script that modifies the DB schema by adding computed union columns.
    /// All registered entity types are processed.
    ///
    String sql();

    /// Produces an SQL script that modifies the DB schema by adding computed union columns.
    /// Only the specified entity types are processed.
    ///
    String sql(Iterable<Class<? extends AbstractEntity<?>>> entityTypes);

    /// Produces an SQL script that modifies the DB schema by adding computed union columns.
    /// Only the specified property is processed.
    /// It is an error if the property is not union-typed.
    ///
    String sql(Class<? extends AbstractEntity<?>> entityType, CharSequence property);

}
