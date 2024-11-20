package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A contract for all audit-entity companion objects to implement.
 *
 * @param <E>  the audited entity type
 * @param <AE>  the audit-entity type
 */
public interface IAuditEntityDao<E extends AbstractEntity<?>, AE extends AbstractAuditEntity<E>>
        extends IEntityDao<AE>, IAuditEntityInstantiator<E, AE>
{

    String ERR_AUDIT_RECORD_DOES_NOT_EXIST = "Audit record does not exist for entity [%s] with ID=%s, version=%s.";

    /**
     * Performs an audit of the specified audited entity instance, which results in:
     * <ul>
     *   <li> An audit record is created by instantiating and saving a corresponding audit-entity.
     *   <li> For each <i>changed property</i>, an audit-prop record is created by instantiating and saving a corresponding
     *        audit-prop entity.
     *        <p>
     *        If the specified audited entity instance is new (i.e., was persisted for the very first time), then properties with
     *        {@code null} values are not considered changed.
     * </ul>
     *
     * @param auditedEntity  the audited entity that will be used to initialise the audit-entity instance.
     *                       Must be persisted and non-dirty.
     * @param transactionGuid  identifier of a transaction that was used to save the audited entity
     * @param dirtyProperties  names of properties of the audited entity whose values changed.
     *                         Only audited properties are considered, others are ignored.
     */
    AE audit(E auditedEntity, String transactionGuid, Iterable<? extends CharSequence> dirtyProperties);

    /**
     * Streams all audit records for an entity with the specified ID.
     *
     * @param auditedEntityId  ID of an audited entity
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    Stream<AE> streamAudits(Long auditedEntityId, @Nullable fetch<AE> fetchModel);

    /**
     * Streams all audit records for an entity with the specified ID using the default fetch model.
     *
     * @param auditedEntityId  ID of an audited entity
     */
    default Stream<AE> streamAudits(final Long auditedEntityId) {
        return streamAudits(auditedEntityId, null);
    }

    /**
     * Streams all audit records for the specified audited entity.
     *
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default Stream<AE> streamAudits(final E auditedEntity, final @Nullable fetch<AE> fetchModel) {
        return streamAudits(auditedEntity.getId(), fetchModel);
    }

    /**
     * Streams all audit records for the specified audited entity using the default fetch model.
     */
    default Stream<AE> streamAudits(final E auditedEntity) {
        return streamAudits(auditedEntity, null);
    }

    /**
     * Streams all audit records for an entity with the specified ID.
     *
     * @param auditedEntityId  ID of an audited entity
     * @param fetchSize  batch size for data retrieval
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    Stream<AE> streamAudits(Long auditedEntityId, int fetchSize, @Nullable fetch<AE> fetchModel);

    /**
     * Streams all audit records for an entity with the specified ID using the default fetch model.
     *
     * @param auditedEntityId  ID of an audited entity
     * @param fetchSize  batch size for data retrieval
     */
    default Stream<AE> streamAudits(final Long auditedEntityId, final int fetchSize) {
        return streamAudits(auditedEntityId, fetchSize, null);
    }

    /**
     * Streams all audit records for the specified audited entity.
     *
     * @param fetchModel  optional fetch model to retrieve audit-entities
     * @param fetchSize  batch size for data retrieval
     */
    default Stream<AE> streamAudits(final E auditedEntity, final int fetchSize, final @Nullable fetch<AE> fetchModel) {
        return streamAudits(auditedEntity.getId(), fetchSize, fetchModel);
    }

    /**
     * Streams all audit records for the specified audited entity using the default fetch model.
     *
     * @param fetchSize  batch size for data retrieval
     */
    default Stream<AE> streamAudits(final E auditedEntity, final int fetchSize) {
        return streamAudits(auditedEntity, fetchSize, null);
    }

    /**
     * Retrieves all audit records for an entity with the specified ID.
     *
     * @param auditedEntityId  ID of an audited entity
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    List<AE> getAudits(Long auditedEntityId, @Nullable fetch<AE> fetchModel);

    /**
     * Retrieves all audit records for an entity with the specified ID using the default fetch model.
     *
     * @param auditedEntityId  ID of an audited entity
     */
    default List<AE> getAudits(final Long auditedEntityId) {
        return getAudits(auditedEntityId, null);
    }

    /**
     * Retrieves all audit records for the specified entity.
     *
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default List<AE> getAudits(final E auditedEntity, final @Nullable fetch<AE> fetchModel) {
        return getAudits(auditedEntity.getId(), fetchModel);
    }

    /**
     * Retrieves all audit records for the specified entity using the default fetch model.
     */
    default List<AE> getAudits(final E auditedEntity) {
        return getAudits(auditedEntity, null);
    }

    /**
     * Retrieves an audit record for an audit entity with the specified ID and version.
     * If an audit record doesn't exist, {@code null} is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param auditedEntityId  ID of an audited entity
     * @param version  version of an audited entity
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    @Nullable AE getAudit(Long auditedEntityId, Long version, @Nullable fetch<AE> fetchModel);

    /**
     * Retrieves an audit record for an audit entity with the specified ID and version using the default fetch model.
     * If an audit record doesn't exist, {@code null} is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param auditedEntityId  ID of an audited entity
     * @param version  version of an audited entity
     */
    default @Nullable AE getAudit(final Long auditedEntityId, final Long version) {
        return getAudit(auditedEntityId, version, null);
    }

    /**
     * Retrieves an audit record for the specified audit entity with the specified version.
     * If an audit record doesn't exist, {@code null} is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param version  version of the specified audited entity, which should be used instead of its current version
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default @Nullable AE getAudit(final E auditedEntity, final Long version, final @Nullable fetch<AE> fetchModel) {
        return getAudit(auditedEntity.getId(), version, fetchModel);
    }

    /**
     * Retrieves an audit record for the specified audit entity with the specified version using the default fetch model.
     * If an audit record doesn't exist, {@code null} is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param version  version of the specified audited entity, which should be used instead of its current version
     */
    default @Nullable AE getAudit(final E auditedEntity, final Long version) {
        return getAudit(auditedEntity, version, null);
    }

    /**
     * Retrieves an audit record for the specified audit entity using its current version.
     * If an audit record doesn't exist, {@code null} is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default @Nullable AE getAudit(final E auditedEntity, final @Nullable fetch<AE> fetchModel) {
        return getAudit(auditedEntity, auditedEntity.getVersion(), fetchModel);
    }

    /**
     * Retrieves an audit record for the specified audit entity using its current version and the default fetch model.
     * If an audit record doesn't exist, {@code null} is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     */
    default @Nullable AE getAudit(final E auditedEntity) {
        return getAudit(auditedEntity, (fetch<AE>) null);
    }

    /**
     * Retrieves an audit record for an audit entity with the specified ID and version.
     * If an audit record doesn't exist, an empty optional is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param auditedEntityId  ID of an audited entity
     * @param version  version of an audited entity
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default Optional<AE> getAuditOptional(final Long auditedEntityId, final Long version, final @Nullable fetch<AE> fetchModel) {
        return Optional.ofNullable(getAudit(auditedEntityId, version, fetchModel));
    }

    /**
     * Retrieves an audit record for an audit entity with the specified ID and version using the default fetch model.
     * If an audit record doesn't exist, an empty optional is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param auditedEntityId  ID of an audited entity
     * @param version  version of an audited entity
     */
    default Optional<AE> getAuditOptional(final Long auditedEntityId, final Long version) {
        return getAuditOptional(auditedEntityId, version, null);
    }

    /**
     * Retrieves an audit record for the specified audit entity with the specified version.
     * If an audit record doesn't exist, an empty optional is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param version  version of the specified audited entity, which should be used instead of its current version
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default Optional<AE> getAuditOptional(final E auditedEntity, final Long version, final @Nullable fetch<AE> fetchModel) {
        return getAuditOptional(auditedEntity.getId(), version, fetchModel);
    }

    /**
     * Retrieves an audit record for the specified audit entity with the specified version using the default fetch model.
     * If an audit record doesn't exist, an empty optional is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param version  version of the specified audited entity, which should be used instead of its current version
     */
    default Optional<AE> getAuditOptional(final E auditedEntity, final Long version) {
        return getAuditOptional(auditedEntity, version, null);
    }

    /**
     * Retrieves an audit record for the specified audit entity using its current version.
     * If an audit record doesn't exist, an empty optional is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default Optional<AE> getAuditOptional(final E auditedEntity, final @Nullable fetch<AE> fetchModel) {
        return getAuditOptional(auditedEntity, auditedEntity.getVersion(), fetchModel);
    }

    /**
     * Retrieves an audit record for the specified audit entity using its current version and the default fetch model.
     * If an audit record doesn't exist, an empty optional is returned.
     * (This can be the case if the specified version is greater than the last persisted version.)
     */
    default Optional<AE> getAuditOptional(final E auditedEntity) {
        return getAuditOptional(auditedEntity, (fetch<AE>) null);
    }

    /**
     * Retrieves an audit record for an audit entity with the specified ID and version.
     * If an audit record doesn't exist, an exception is thrown.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param auditedEntityId  ID of an audited entity
     * @param version  version of an audited entity
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default AE getAuditOrThrow(final Long auditedEntityId, final Long version, final @Nullable fetch<AE> fetchModel) {
        final var audit = getAudit(auditedEntityId, version, fetchModel);
        if (audit == null) {
            throw new EntityCompanionException(ERR_AUDIT_RECORD_DOES_NOT_EXIST.formatted(
                    AuditUtils.getAuditedType(getEntityType()).getSimpleName(), auditedEntityId, version));
        }
        return audit;
    }

    /**
     * Retrieves an audit record for an audit entity with the specified ID and version using the default fetch model.
     * If an audit record doesn't exist, an exception is thrown.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param auditedEntityId  ID of an audited entity
     * @param version  version of an audited entity
     */
    default AE getAuditOrThrow(final Long auditedEntityId, final Long version) {
        return getAuditOrThrow(auditedEntityId, version, null);
    }

    /**
     * Retrieves an audit record for the specified audit entity with the specified version.
     * If an audit record doesn't exist, an exception is thrown.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param version  version of the specified audited entity that is used instead of its current version
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default AE getAuditOrThrow(final E auditedEntity, final Long version, final @Nullable fetch<AE> fetchModel) {
        return getAuditOrThrow(auditedEntity.getId(), version, fetchModel);
    }

    /**
     * Retrieves an audit record for the specified audit entity with the specified version using the default fetch model.
     * If an audit record doesn't exist, an exception is thrown.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param version  version of the specified audited entity that is used instead of its current version
     */
    default AE getAuditOrThrow(final E auditedEntity, final Long version) {
        return getAuditOrThrow(auditedEntity, version, null);
    }

    /**
     * Retrieves an audit record for the specified audit entity using its current version.
     * If an audit record doesn't exist, an exception is thrown.
     * (This can be the case if the specified version is greater than the last persisted version.)
     *
     * @param fetchModel  optional fetch model to retrieve audit-entities
     */
    default AE getAuditOrThrow(final E auditedEntity, final @Nullable fetch<AE> fetchModel) {
        return getAuditOrThrow(auditedEntity, auditedEntity.getVersion(), fetchModel);
    }

    /**
     * Retrieves an audit record for the specified audit entity using its current version and the default fetch model.
     * If an audit record doesn't exist, an exception is thrown.
     * (This can be the case if the specified version is greater than the last persisted version.)
     */
    default AE getAuditOrThrow(final E auditedEntity) {
        return getAuditOrThrow(auditedEntity, (fetch<AE>) null);
    }

}
