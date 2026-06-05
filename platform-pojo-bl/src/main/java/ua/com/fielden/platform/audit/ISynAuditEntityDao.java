package ua.com.fielden.platform.audit;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/// A contract for all synthetic audit-entity companion objects to implement.
///
/// This is the primary interface for application developers to access audit data and perform manual auditing if necessary.
///
/// @param <E>  the audited entity type
/// @see IEntityAuditor
///
public interface ISynAuditEntityDao<E extends AbstractEntity<?>>
        extends IEntityDao<AbstractSynAuditEntity<E>>,
                IEntityAuditor<E>
{

    /// Streams all audit records for an entity with the specified ID.
    ///
    /// The returned stream must be closed to ensure that the underlying resultset is closed.
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    Stream<AbstractSynAuditEntity<E>> streamAudits(Long auditedEntityId, @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel);

    /// Streams all audit records for an entity with the specified ID using the default fetch model.
    ///
    /// The returned stream must be closed to ensure that the underlying resultset is closed.
    ///
    /// @param auditedEntityId  ID of an audited entity
    ///
    default Stream<AbstractSynAuditEntity<E>> streamAudits(final Long auditedEntityId) {
        return streamAudits(auditedEntityId, null);
    }

    /// Streams all audit records for the specified audited entity.
    ///
    /// The returned stream must be closed to ensure that the underlying resultset is closed.
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    default Stream<AbstractSynAuditEntity<E>> streamAudits(final E auditedEntity, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return streamAudits(auditedEntity.getId(), fetchModel);
    }

    /// Streams all audit records for the specified audited entity using the default fetch model.
    ///
    /// The returned stream must be closed to ensure that the underlying resultset is closed.
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    ///
    default Stream<AbstractSynAuditEntity<E>> streamAudits(final E auditedEntity) {
        return streamAudits(auditedEntity, null);
    }

    /// Streams all audit records for an entity with the specified ID.
    ///
    /// The returned stream must be closed to ensure that the underlying resultset is closed.
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param fetchSize  batch size for data retrieval
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    Stream<AbstractSynAuditEntity<E>> streamAudits(Long auditedEntityId, int fetchSize, @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel);

    /// Streams all audit records for an entity with the specified ID using the default fetch model.
    ///
    /// The returned stream must be closed to ensure that the underlying resultset is closed.
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param fetchSize  batch size for data retrieval
    ///
    default Stream<AbstractSynAuditEntity<E>> streamAudits(final Long auditedEntityId, final int fetchSize) {
        return streamAudits(auditedEntityId, fetchSize, null);
    }

    /// Streams all audit records for the specified audited entity.
    ///
    /// The returned stream must be closed to ensure that the underlying resultset is closed.
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    /// @param fetchSize  batch size for data retrieval
    ///
    default Stream<AbstractSynAuditEntity<E>> streamAudits(final E auditedEntity, final int fetchSize, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return streamAudits(auditedEntity.getId(), fetchSize, fetchModel);
    }

    /// Streams all audit records for the specified audited entity using the default fetch model.
    ///
    /// The returned stream must be closed to ensure that the underlying resultset is closed.
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param fetchSize  batch size for data retrieval
    ///
    default Stream<AbstractSynAuditEntity<E>> streamAudits(final E auditedEntity, final int fetchSize) {
        return streamAudits(auditedEntity, fetchSize, null);
    }

    /// Retrieves all audit records for an entity with the specified ID.
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    List<AbstractSynAuditEntity<E>> getAudits(Long auditedEntityId, @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel);

    /// Retrieves all audit records for an entity with the specified ID using the default fetch model.
    ///
    /// @param auditedEntityId  ID of an audited entity
    ///
    default List<AbstractSynAuditEntity<E>> getAudits(final Long auditedEntityId) {
        return getAudits(auditedEntityId, null);
    }

    /// Retrieves all audit records for the specified entity.
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    default List<AbstractSynAuditEntity<E>> getAudits(final E auditedEntity, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return getAudits(auditedEntity.getId(), fetchModel);
    }

    /// Retrieves all audit records for the specified entity using the default fetch model.
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    ///
    default List<AbstractSynAuditEntity<E>> getAudits(final E auditedEntity) {
        return getAudits(auditedEntity, null);
    }

    /// Retrieves an audit record for an audited entity with the specified ID and version.
    /// If an audit record doesn't exist, `null` is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param version  version of an audited entity
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    @Nullable AbstractSynAuditEntity<E> getAudit(Long auditedEntityId, Long version, @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel);

    /// Retrieves an audit record for an audited entity with the specified ID and version using the default fetch model.
    /// If an audit record doesn't exist, `null` is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param version  version of an audited entity
    ///
    default @Nullable AbstractSynAuditEntity<E> getAudit(final Long auditedEntityId, final Long version) {
        return getAudit(auditedEntityId, version, null);
    }

    /// Retrieves an audit record for the specified audited entity with the specified version.
    /// If an audit record doesn't exist, `null` is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param version  version of the specified audited entity, which should be used instead of its current version
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    default @Nullable AbstractSynAuditEntity<E> getAudit(final E auditedEntity, final Long version, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return getAudit(auditedEntity.getId(), version, fetchModel);
    }

    /// Retrieves an audit record for the specified audited entity with the specified version using the default fetch model.
    /// If an audit record doesn't exist, `null` is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param version  version of the specified audited entity, which should be used instead of its current version
    ///
    default @Nullable AbstractSynAuditEntity<E> getAudit(final E auditedEntity, final Long version) {
        return getAudit(auditedEntity, version, null);
    }

    /// Retrieves an audit record for the specified audited entity using its current version.
    /// If an audit record doesn't exist, `null` is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have properties `id` and `version`
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    default @Nullable AbstractSynAuditEntity<E> getAudit(final E auditedEntity, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return getAudit(auditedEntity, auditedEntity.getVersion(), fetchModel);
    }

    /// Retrieves an audit record for the specified audited entity using its current version and the default fetch model.
    /// If an audit record doesn't exist, `null` is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have properties `id` and `version`
    ///
    default @Nullable AbstractSynAuditEntity<E> getAudit(final E auditedEntity) {
        return getAudit(auditedEntity, (fetch<AbstractSynAuditEntity<E>>) null);
    }

    /// Retrieves an audit record for an audited entity with the specified ID and version.
    /// If an audit record doesn't exist, an empty optional is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param version  version of an audited entity
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    default Optional<AbstractSynAuditEntity<E>> getAuditOptional(final Long auditedEntityId, final Long version, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return Optional.ofNullable(getAudit(auditedEntityId, version, fetchModel));
    }

    /// Retrieves an audit record for an audited entity with the specified ID and version using the default fetch model.
    /// If an audit record doesn't exist, an empty optional is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param version  version of an audited entity
    ///
    default Optional<AbstractSynAuditEntity<E>> getAuditOptional(final Long auditedEntityId, final Long version) {
        return getAuditOptional(auditedEntityId, version, null);
    }

    /// Retrieves an audit record for the specified audited entity with the specified version.
    /// If an audit record doesn't exist, an empty optional is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param version  version of the specified audited entity, which should be used instead of its current version
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    default Optional<AbstractSynAuditEntity<E>> getAuditOptional(final E auditedEntity, final Long version, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return getAuditOptional(auditedEntity.getId(), version, fetchModel);
    }

    /// Retrieves an audit record for the specified audited entity with the specified version using the default fetch model.
    /// If an audit record doesn't exist, an empty optional is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param version  version of the specified audited entity, which should be used instead of its current version
    ///
    default Optional<AbstractSynAuditEntity<E>> getAuditOptional(final E auditedEntity, final Long version) {
        return getAuditOptional(auditedEntity, version, null);
    }

    /// Retrieves an audit record for the specified audited entity using its current version.
    /// If an audit record doesn't exist, an empty optional is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have properties `id` and `version`
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    default Optional<AbstractSynAuditEntity<E>> getAuditOptional(final E auditedEntity, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return getAuditOptional(auditedEntity, auditedEntity.getVersion(), fetchModel);
    }

    /// Retrieves an audit record for the specified audited entity using its current version and the default fetch model.
    /// If an audit record doesn't exist, an empty optional is returned.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have properties `id` and `version`
    ///
    default Optional<AbstractSynAuditEntity<E>> getAuditOptional(final E auditedEntity) {
        return getAuditOptional(auditedEntity, (fetch<AbstractSynAuditEntity<E>>) null);
    }

    /// Retrieves an audit record for an audited entity with the specified ID and version.
    /// If an audit record doesn't exist, an exception is thrown.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param version  version of an audited entity
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    AbstractSynAuditEntity<E> getAuditOrThrow(final Long auditedEntityId, final Long version, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel);

    /// Retrieves an audit record for an audited entity with the specified ID and version using the default fetch model.
    /// If an audit record doesn't exist, an exception is thrown.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntityId  ID of an audited entity
    /// @param version  version of an audited entity
    ///
    default AbstractSynAuditEntity<E> getAuditOrThrow(final Long auditedEntityId, final Long version) {
        return getAuditOrThrow(auditedEntityId, version, null);
    }

    /// Retrieves an audit record for the specified audited entity with the specified version.
    /// If an audit record doesn't exist, an exception is thrown.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param version  version of the specified audited entity that is used instead of its current version
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    default AbstractSynAuditEntity<E> getAuditOrThrow(final E auditedEntity, final Long version, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return getAuditOrThrow(auditedEntity.getId(), version, fetchModel);
    }

    /// Retrieves an audit record for the specified audited entity with the specified version using the default fetch model.
    /// If an audit record doesn't exist, an exception is thrown.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have property `id`
    /// @param version  version of the specified audited entity that is used instead of its current version
    ///
    default AbstractSynAuditEntity<E> getAuditOrThrow(final E auditedEntity, final Long version) {
        return getAuditOrThrow(auditedEntity, version, null);
    }

    /// Retrieves an audit record for the specified audited entity using its and current version.
    /// If an audit record doesn't exist, an exception is thrown.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have properties `id` and `version`
    /// @param fetchModel  optional fetch model to retrieve audit-entities
    ///
    default AbstractSynAuditEntity<E> getAuditOrThrow(final E auditedEntity, final @Nullable fetch<AbstractSynAuditEntity<E>> fetchModel) {
        return getAuditOrThrow(auditedEntity, auditedEntity.getVersion(), fetchModel);
    }

    /// Retrieves an audit record for the specified audited entity using its current version and the default fetch model.
    /// If an audit record doesn't exist, an exception is thrown.
    /// (This can be the case if the specified version is greater than the last audited version.)
    ///
    /// @param auditedEntity  audited entity, must have properties `id` and `version`
    ///
    default AbstractSynAuditEntity<E> getAuditOrThrow(final E auditedEntity) {
        return getAuditOrThrow(auditedEntity, (fetch<AbstractSynAuditEntity<E>>) null);
    }

}
