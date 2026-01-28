package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/// A contract to generate insert SQL statements for audit-prop entities.
///
/// This interface belongs to internal API.
/// Cannot be used if auditing is disabled.
///
/// @param <E>  the audited entity type
///
interface IAuditPropInstantiator<E extends AbstractEntity<?>> {


    /// Returns an insert SQL statement for a new audit-prop entity.
    ///
    String sqlInsertAuditPropStmt();

    /// Returns a property descriptor for `propName`,
    /// belonging to a synthetic entity that corresponds to the audit entity type `E`.
    ///
    PropertyDescriptor<? extends AbstractSynAuditEntity<E>> pd(final String propName);
}
