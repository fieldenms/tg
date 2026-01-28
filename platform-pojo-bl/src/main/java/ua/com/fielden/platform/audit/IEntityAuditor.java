package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

import java.util.Collection;

/// A contract to perform auditing of entities.
///
/// @param <E>  the audited entity type
///
public interface IEntityAuditor<E extends AbstractEntity<?>> {

    /// Performs an audit of the audited entity, identified by its ID.
    /// This results in:
    ///
    /// -  An audit record is created.
    /// -  For each _changed property_, an audit-prop record is created.
    ///
    ///    It is considered that no property were changed for newly persisted entity instances.
    ///    Therefore, no audit-prop records get created for such instances.
    ///
    /// If none of the audited properties are dirty, auditing is not performed.
    ///
    /// This method requires a session but is deliberately not annotated with `@SessionRequired`, which must also be the case for its implementation.
    /// This enforces the contract that this method may only be used as a part of a save operation on an audited entity.
    ///
    /// @param auditedEntityId       the ID of an audited entity
    /// @param auditedEntityVersion  the version of an audited entity
    /// @param transactionGuid       identifier of a transaction that was used to save the audited entity
    /// @param dirtyProperties       names of properties of the audited entity whose values changed;
    ///                              only audited properties are considered, others are ignored.
    ///
    void audit(Long auditedEntityId, Long auditedEntityVersion, String transactionGuid, Collection<String> dirtyProperties);

    /// Returns a fetch model for the audited entity type that includes all properties that are necessary to perform auditing.
    ///
    fetch<E> fetchModelForAuditing();

}
