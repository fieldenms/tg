package ua.com.fielden.platform.web.audit;

import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;

import java.util.Optional;

import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.AUDITED_ENTITY;
import static ua.com.fielden.platform.dao.AbstractOpenCompoundMasterDao.enhanceEmbededCentreQuery;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createConditionProperty;

/// Standard query enhancer for synthetic audit-entity centres embedded in masters of their respective audited entities.
///
public class AuditEntityQueryEnhancer implements IQueryEnhancer<AbstractSynAuditEntity<?>> {

    /// Returns this class as a query ehnancer parameterised with the desired synthetic audit-entity type.
    ///
    /// Designed as an alternative to `.class` with type casts.
    ///
    public static <E extends AbstractSynAuditEntity<?>> Class<IQueryEnhancer<E>> klass() {
        return (Class) AuditEntityQueryEnhancer.class;
    }

    @Override
    public EntityQueryProgressiveInterfaces.ICompleted<AbstractSynAuditEntity<?>> enhanceQuery(
            final EntityQueryProgressiveInterfaces.IWhere0<AbstractSynAuditEntity<?>> where,
            final Optional<CentreContext<AbstractSynAuditEntity<?>, ?>> context)
    {
        return enhanceEmbededCentreQuery(where,
                                         createConditionProperty(AUDITED_ENTITY),
                                         context.get().getMasterEntity().getKey());
    }

}
