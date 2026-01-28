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

    /// Returns a property descriptor for `propName`, which represents a property of a synthetic audit entity type that
    /// corresponds to the audited entity type `E`.
    ///
    /// @param propName  the name of a property of the synthetic audit entity type
    ///
    PropertyDescriptor<? extends AbstractSynAuditEntity<E>> pd(String propName);

    /// Returns the associated audit-prop entity type.
    ///
    Class<? extends AbstractAuditProp<E>> auditPropEntityType();

}
