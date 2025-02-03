package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import static java.lang.String.join;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Static utitlies used by generated synthetic audit-prop entities.
 */
public final class SynAuditPropEntityUtils {

    public static <E extends AbstractSynAuditProp<?>> EntityResultQueryModel<E> modelAuditProp(
            final Class<? extends AbstractAuditProp<?>> auditPropType,
            final Class<E> synAuditPropType,
            final Class<? extends AbstractAuditEntity<?>> auditType,
            final Class<? extends AbstractSynAuditEntity<?>> synAuditType)
    {
        return select(auditPropType)
                .yield().prop(ID).as(ID)
                .yield().prop(VERSION).as(VERSION)
                .yield()
                    .model(select(auditType).where().prop(ID).eq().extProp(join(".", AbstractAuditProp.AUDIT_ENTITY, ID)).modelAsEntity(synAuditType))
                    .as(AbstractSynAuditProp.AUDIT_ENTITY)
                .yield().prop(AbstractAuditProp.PROPERTY).as(AbstractSynAuditProp.PROPERTY)
                .modelAsEntity(synAuditPropType);
    }

}
