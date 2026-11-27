package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.dao.AbstractEntityDao;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/// Generic type for synthetic audit-prop entity companion objects.
/// This type should be instantiated directly.
/// Unlike [CommonEntityDao], this type does not have to be subclassed.
///
/// @param <E>  the audited entity type
///
public class CommonSynAuditPropDao<E extends AbstractEntity<?>> extends AbstractEntityDao<AbstractSynAuditProp<E>> {

    // All fields below are effectively final, but cannot be declared so due to late initialisation.

    private Class<AbstractSynAuditProp<E>> entityType;
    private Class<? extends Comparable<?>> keyType;

    protected void init(
            final Class<AbstractSynAuditProp<E>> entityType,
            final Class<? extends Comparable<?>> keyType)
    {
        this.entityType = entityType;
        this.keyType = keyType;
    }

    @Override
    public Class<AbstractSynAuditProp<E>> getEntityType() {
        return entityType;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        return keyType;
    }

}
