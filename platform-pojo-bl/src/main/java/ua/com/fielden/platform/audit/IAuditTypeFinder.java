package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/// Locates audit types.
///
/// The `navigate*` group of methods accept either an audited type or an audit type, and the resulting [Navigator] can be used
/// to "navigate" to some other type in the context.
/// For each audited type `E`, there is a context represented by the set of `E`'s audit types and `E` itself.
///
/// Cannot be used if auditing is disabled, all methods will throw [AuditingModeException].
///
public interface IAuditTypeFinder {

    /// Navigates to the specified entity type, which must be audited.
    ///
    /// @see Navigator
    ///
    <E extends AbstractEntity<?>> Navigator<E> navigate(Class<E> type);

    /// Navigates to the specified persistent audit-entity type.
    ///
    /// @see Navigator
    ///
    <E extends AbstractEntity<?>> Navigator<E> navigateAudit(Class<AbstractAuditEntity<E>> type);

    /// Navigates to the specified persistent audit-prop type.
    ///
    /// @see Navigator
    ///
    <E extends AbstractEntity<?>> Navigator<E> navigateAuditProp(Class<AbstractAuditProp<E>> type);

    /// Navigates to the specified synthetic audit-entity type.
    ///
    /// @see Navigator
    ///
    <E extends AbstractEntity<?>> Navigator<E> navigateSynAudit(Class<AbstractSynAuditEntity<E>> type);

    /// Navigates to the specified synthetic audit-prop type.
    ///
    /// @see Navigator
    ///
    <E extends AbstractEntity<?>> Navigator<E> navigateSynAuditProp(Class<AbstractSynAuditProp<E>> type);

    /// Provides a uniform API for accessing an audited type `E` and all of its associated audit types.
    ///
    /// It supports:
    /// * locating synthetic and persistent audit-entity and audit-prop types;
    /// * selecting a specific audit type by version;
    /// * obtaining all related audit types in a stable, version-aware order.
    ///
    /// Handling of exceptional situations depends on the active [AuditingMode].
    /// When the auditing mode is [AuditingMode#ENABLED], all expected audit types are required to exist.
    /// When the auditing mode is [AuditingMode#GENERATION], some or all audit types may legitimately be absent.
    ///
    /// @param <E> the audited entity type
    ///
    interface Navigator<E extends AbstractEntity<?>> {

        /// Returns the audited type.
        ///
        Class<E> auditedType();

        /// Returns the synthetic audit-entity type, which must exist.
        ///
        Class<AbstractSynAuditEntity<E>> synAuditEntityType();

        /// Returns the synthetic audit-entity type, which may not exist.
        ///
        Optional<Class<AbstractSynAuditEntity<E>>> findSynAuditEntityType();

        /// Returns the synthetic audit-prop type, which must exist.
        ///
        Class<AbstractSynAuditProp<E>> synAuditPropType();

        /// Returns the synthetic audit-prop type, which may not exist.
        ///
        Optional<Class<AbstractSynAuditProp<E>>> findSynAuditPropType();

        /// Returns all persistent audit-entity types.
        /// If the auditing mode is [#ENABLED], the returned collection is never empty.
        /// If the auditing mode is [#GENERATION], the returned collection may be empty.
        ///
        Collection<Class<AbstractAuditEntity<E>>> allAuditEntityTypes();

        /// Returns the latest persistent audit-entity type (i.e., the one with the greatest version), which must exist.
        ///
        Class<AbstractAuditEntity<E>> auditEntityType();

        /// Returns the latest persistent audit-entity type (i.e., the one with the greatest version), which may not exist.
        ///
        Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType();

        /// Returns an audit-entity type with the specified version, which must exist.
        ///
        /// @see AuditUtils#getAuditTypeVersion(Class)
        ///
        Class<AbstractAuditEntity<E>> auditEntityType(int version);

        /// Returns an audit-entity type with the specified version, which may not exist.
        ///
        /// @see AuditUtils#getAuditTypeVersion(Class)
        ///
        Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType(int version);

        /// If the auditing mode is [#ENABLED], the returned collection is never empty.
        /// If the auditing mode is [#GENERATION], the returned collection may be empty.
        ///
        Collection<Class<AbstractAuditProp<E>>> allAuditPropTypes();

        /// Returns the latest persistent audit-prop type (i.e., the one with the greatest version), which must exist.
        ///
        Class<AbstractAuditProp<E>> auditPropType();

        /// Returns the latest persistent audit-prop type (i.e., the one with the greatest version), which may not exist.
        ///
        Optional<Class<AbstractAuditProp<E>>> findAuditPropType();

        /// Returns an audit-prop type with the specified version, which must exist.
        ///
        /// @see AuditUtils#getAuditTypeVersion(Class)
        ///
        Class<AbstractAuditProp<E>> auditPropType(int version);

        /// Returns an audit-prop type with the specified version, which may not exist.
        ///
        /// @see AuditUtils#getAuditTypeVersion(Class)
        ///
        Optional<Class<AbstractAuditProp<E>>> findAuditPropType(int version);

        /// Returns all persistent audit types.
        /// The result is equivalent to the union of [#allAuditEntityTypes()] and [#allAuditPropTypes()].
        /// For convenience, the result is sorted by audit type version ascending (e.g., `Entity_a3t_1`, `Entity_a3t_1_Prop`, `Entity_a3t_2`).
        List<Class<? extends AbstractEntity<?>>> allPersistentAuditTypes();

        /// Returns all audit types.
        ///
        Collection<Class<? extends AbstractEntity<?>>> allAuditTypes();

    }

}
