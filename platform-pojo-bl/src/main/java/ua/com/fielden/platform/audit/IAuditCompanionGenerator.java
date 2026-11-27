package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.dao.IEntityDao;

/// This interface defines the generator of audit-entity companion objects.
///
/// Cannot be used if auditing is disabled, all methods will throw [AuditingModeException].
///
/// Each invocation creates a new companion object.
///
public interface IAuditCompanionGenerator {

    /// Creates a companion object for the specified audit-entity type.
    ///
    <E extends AbstractAuditEntity<?>> IEntityDao<E> generateCompanion(Class<E> type);

    /// Creates a companion object for the specified audit-prop entity type.
    ///
    <E extends AbstractAuditProp<?>> IEntityDao<E> generateCompanionForAuditProp(Class<E> type);

    /// Creates a companion object for the specified synthetic audit-entity type.
    ///
    /// The companion object implements [ISynAuditEntityDao].
    ///
    <E extends AbstractSynAuditEntity<?>> IEntityDao<E> generateCompanionForSynAuditEntity(Class<E> type);

    /// Creates a companion object for the specified synthetic audit-prop entity type.
    ///
    <E extends AbstractSynAuditProp<?>> IEntityDao<E> generateCompanionForSynAuditProp(Class<E> type);

}
