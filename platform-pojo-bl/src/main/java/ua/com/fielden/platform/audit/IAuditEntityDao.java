package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for all audit-entity companion objects to implement.
 *
 * @param <E>  the audited entity type
 * @param <AE>  the audit-entity type
 */
public interface IAuditEntityDao<E extends AbstractEntity<?>, AE extends AbstractAuditEntity<E>>
        extends IEntityDao<AE>, IAuditEntityInstantiator<E, AE>
{

    /**
     * Performs an audit of the specified audited entity instance, which results in:
     * <ul>
     *   <li> An audit record is created by instantiating and saving a corresponding audit-entity.
     *   <li> For each <i>changed property</i>, an audit-prop record is created by instantiating and saving a corresponding
     *        audit-prop entity.
     * </ul>
     *
     * @param auditedEntity  the audited entity that will be used to initialise the audit-entity instance.
     *                       Must be persisted and non-dirty.
     * @param transactionGuid  identifier of a transaction that was used to save the audited entity
     * @param dirtyProperties  names of properties of the audited entity whose values changed.
     *                         Only audited properties are considered, others are ignored.
     */
    AE audit(E auditedEntity, String transactionGuid, Iterable<? extends CharSequence> dirtyProperties);

}
