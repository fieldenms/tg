package ua.com.fielden.platform.audit;

import jakarta.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.utils.EntityUtils;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.audit.AuditUtils.getAuditTypeForAuditPropType;

/**
 * Base type for implementations of audit-prop entity companion objects.
 *
 * @param <AE>  the audit-entity type
 * @param <AP>  the audit-prop entity type
 */
public abstract class CommonAuditPropDao<AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>>
        extends CommonEntityDao<AP>
        implements IAuditPropDao<AE, AP>
{

    private final Class<AE> auditEntityType;

    private IDomainMetadata domainMetadata;

    protected CommonAuditPropDao() {
        super();
        auditEntityType = getAuditTypeForAuditPropType(getEntityType());
    }

    @Inject
    protected void setDomainMetadata(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    @Override
    public AP newAuditProp(final AE auditEntity, final CharSequence property) {
        final var auditProp = new_();

        auditProp.setAuditEntity(auditEntity);
        auditProp.setProperty(PropertyDescriptor.pd(auditEntityType, property.toString()));

        return auditProp;
    }

    @Override
    protected IFetchProvider<AP> createFetchProvider() {
        return EntityUtils.fetch(getEntityType())
                .with(domainMetadata.forEntity(getEntityType()).properties().stream()
                              .filter(PropertyMetadata::isPersistent)
                              .map(PropertyMetadata::name)
                              .collect(toSet()));
    }

}
