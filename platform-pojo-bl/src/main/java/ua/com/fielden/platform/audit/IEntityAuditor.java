package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to perform auditing of entities.
 *
 * @param <E>  the audited entity type
 */
public interface IEntityAuditor<E extends AbstractEntity<?>> {

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
     * <p>
     * This method requires a session but is deliberately not annotated with {@code @SessionRequired}, which must also be the case for its implementation.
     * This enforces the contract that this method may only be used as a part of a save operation on an audited entity.
     *
     * @param auditedEntity  the audited entity that will be used to initialise the audit-entity instance.
     *                       Must be persisted and non-dirty.
     * @param transactionGuid  identifier of a transaction that was used to save the audited entity
     * @param dirtyProperties  names of properties of the audited entity whose values changed.
     *                         Only audited properties are considered, others are ignored.
     */
    void audit(E auditedEntity, String transactionGuid, Iterable<? extends CharSequence> dirtyProperties);

}
