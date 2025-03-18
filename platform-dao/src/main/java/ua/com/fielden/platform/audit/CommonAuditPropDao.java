package ua.com.fielden.platform.audit;

import jakarta.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.utils.EntityUtils;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.audit.AuditUtils.getAuditTypeForAuditPropType;
import static ua.com.fielden.platform.audit.AuditUtils.getAuditedType;
import static ua.com.fielden.platform.entity.exceptions.NoSuchPropertyException.noSuchPropertyException;

/**
 * Base type for implementations of audit-prop entity companion objects.
 *
 * @param <E>  the audited entity type
 */
public abstract class CommonAuditPropDao<E extends AbstractEntity<?>>
        extends CommonEntityDao<AbstractAuditProp<E>>
        implements IAuditPropInstantiator<E>
{

    private final Class<AbstractAuditEntity<E>> auditEntityType;
    private Class<AbstractSynAuditEntity<E>> synAuditEntityType;

    private IDomainMetadata domainMetadata;

    protected CommonAuditPropDao() {
        super();
        auditEntityType = getAuditTypeForAuditPropType((Class<AbstractAuditProp<E>>) getEntityType());
    }

    @Inject
    protected void setDomainMetadata(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    @Inject
    protected void setAuditTypeFinder(final IAuditTypeFinder auditTypeFinder) {
        final var auditedType = getAuditedType(auditEntityType);
        synAuditEntityType = auditTypeFinder.getSynAuditEntityType(auditedType);
    }

    @Override
    public AbstractAuditProp<E> newAuditProp(final AbstractAuditEntity<E> auditEntity, final CharSequence property) {
        if (!(auditEntity.isPersisted() && !auditEntity.isDirty())) {
            throw new InvalidArgumentException("Audit-entity must be persisted and non-dirty.");
        }
        if (domainMetadata.forPropertyOpt(auditEntityType, property).isEmpty()) {
            throw noSuchPropertyException(auditEntityType, property);
        }

        final AbstractAuditProp<E> auditProp = new_();

        auditProp.setAuditEntity(auditEntity);
        auditProp.setProperty(PropertyDescriptor.pd(synAuditEntityType, property.toString()));

        return auditProp;
    }

    @Override
    public AbstractAuditProp<E> fastNewAuditProp(final AbstractAuditEntity<E> auditEntity, final CharSequence property) {
        final AbstractAuditProp<E> auditProp = new_();
        auditProp.beginInitialising();

        auditProp.setAuditEntity(auditEntity);
        auditProp.setProperty(PropertyDescriptor.pd(synAuditEntityType, property.toString()));

        auditProp.endInitialising();

        return auditProp;
    }

    @Override
    protected IFetchProvider<AbstractAuditProp<E>> createFetchProvider() {
        return EntityUtils.fetch(getEntityType())
                .with(domainMetadata.forEntity(getEntityType()).properties().stream()
                              .filter(PropertyMetadata::isPersistent)
                              .map(PropertyMetadata::name)
                              .collect(toSet()));
    }

}
