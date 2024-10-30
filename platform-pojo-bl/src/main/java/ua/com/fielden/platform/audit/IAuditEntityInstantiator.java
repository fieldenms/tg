package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to instantiate audit-entities.
 *
 * @param <E>  the audited entity type
 * @param <AE>  the audit-entity type
 */
public interface IAuditEntityInstantiator<E extends AbstractEntity<?>, AE extends AbstractAuditEntity<E>> {

    /**
     * Returns a new, initialised instance of this audit-entity type.
     *
     * @param auditedEntity  the audited entity that will be used to initialise the audit-entity instance.
     *                       Must be persisted and non-dirty.
     * @param transactionGuid  identifier of a transaction that was used to save the audited entity
     */
    AE newAudit(E auditedEntity, String transactionGuid);

}
