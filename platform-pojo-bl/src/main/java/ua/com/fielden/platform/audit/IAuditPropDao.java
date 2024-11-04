package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * A contract for all audit-prop entity companion objects to implement.
 *
 * @param <AE>  the audit-entity type
 * @param <AP>  the audit-prop entity type
 */
public interface IAuditPropDao<AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>>
        extends IEntityDao<AP>, IAuditPropInstantiator<AE, AP>
{}
