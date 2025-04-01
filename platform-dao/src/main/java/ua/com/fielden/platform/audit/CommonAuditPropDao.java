package ua.com.fielden.platform.audit;

import jakarta.inject.Inject;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Objects;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.exceptions.NoSuchPropertyException.noSuchPropertyException;

/**
 * Base type for implementations of audit-prop entity companion objects.
 * <p>
 * Cannot be used if auditing is disabled. Will throw {@link AuditingModeException} upon construction.
 *
 * @param <E>  the audited entity type
 */
public abstract class CommonAuditPropDao<E extends AbstractEntity<?>>
        extends CommonEntityDao<AbstractAuditProp<E>>
        implements IAuditPropInstantiator<E>
{

    private Class<AbstractAuditEntity<E>> auditEntityType;
    private Class<AbstractSynAuditEntity<E>> synAuditEntityType;

    private IDomainMetadata domainMetadata;

    protected CommonAuditPropDao() {
        super();
    }

    @Inject
    protected void init(
            final AuditingMode auditingMode,
            final IDomainMetadata domainMetadata,
            final IAuditTypeFinder auditTypeFinder)
    {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(this.getClass(), auditingMode);
        }

        this.domainMetadata = domainMetadata;
        final var navigator = auditTypeFinder.navigateAuditProp(getEntityType());
        auditEntityType = navigator.auditEntityType();
        synAuditEntityType = navigator.synAuditEntityType();

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
