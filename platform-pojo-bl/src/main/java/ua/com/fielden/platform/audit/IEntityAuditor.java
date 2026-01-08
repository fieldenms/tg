package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Collection;

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
     * If none of the audited properties are dirty, auditing is not performed.
     * <p>
     * This method requires a session but is deliberately not annotated with {@code @SessionRequired}, which must also be the case for its implementation.
     * This enforces the contract that this method may only be used as a part of a save operation on an audited entity.
     *
     * @param auditedEntityOrId  either an audited entity that will be used to initialise the audit-entity instance (must be persisted, not dirty, and valid),
     *                           or the ID of that entity.
     *                           If any of the audited properties are proxied, the entity will be refetched.
     * @param transactionGuid  identifier of a transaction that was used to save the audited entity
     * @param dirtyProperties  names of properties of the audited entity whose values changed.
     *                         Only audited properties are considered, others are ignored.
     */
    void audit(Either<Long, E> auditedEntityOrId, String transactionGuid, Collection<String> dirtyProperties);

    /**
     * Returns a fetch model for the audited entity type that includes all properties that are necessary to perform auditing.
     */
    fetch<E> fetchModelForAuditing();

}
