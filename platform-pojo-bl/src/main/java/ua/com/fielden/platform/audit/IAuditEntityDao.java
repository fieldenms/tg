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
{}
