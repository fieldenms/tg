package ua.com.fielden.platform.audit;

import com.google.common.collect.Iterables;
import jakarta.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;

import javax.annotation.Nullable;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.A3T;
import static ua.com.fielden.platform.audit.AuditUtils.getAuditPropTypeForAuditType;
import static ua.com.fielden.platform.error.Result.failuref;

/**
 * Base type for implementations of audit-entity companion objects.
 *
 * @param <E>  the audited entity type
 * @param <AE>  the audit-entity type
 */
public abstract class CommonAuditEntityDao<E extends AbstractEntity<?>, AE extends AbstractAuditEntity<E>>
        extends CommonEntityDao<AE>
        implements IAuditEntityDao<E, AE>
{
    private final Class<AbstractAuditProp<AE>> auditPropType;

    private IDomainMetadata domainMetadata;

    protected CommonAuditEntityDao() {
        super();
        auditPropType = getAuditPropTypeForAuditType(getEntityType());
    }

    @Inject
    protected void setDomainMetadata(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    /**
     * Returns the name of a property of this audit-entity type that audits the specified property of the audited entity type,
     * if the specified property is indeed audited; otherwise, returns {@code null}.
     */
    protected final @Nullable String getAuditPropertyName(final CharSequence auditedProperty) {
        final var auditPropertyName = A3T + "_" + auditedProperty;
        return domainMetadata.forEntity(getEntityType()).propertyOpt(auditPropertyName).isPresent() ? auditPropertyName : null;
        // return auditedToAuditPropertyNames.get(auditedProperty.toString());
    }

    @Override
    public AE audit(final E auditedEntity, final String transactionGuid, final Iterable<? extends CharSequence> dirtyProperties) {
        final AE auditEntity = save(newAudit(auditedEntity, transactionGuid));

        if (!Iterables.isEmpty(dirtyProperties)) {
            // Audit information about changed properites
            final IAuditPropDao<AE, AbstractAuditProp<AE>> coAuditProp = co(auditPropType);
            for (final var property : dirtyProperties) {
                final var auditProperty = getAuditPropertyName(property.toString());
                if (auditProperty != null) {
                    coAuditProp.quickSave(coAuditProp.newAuditProp(auditEntity, auditProperty));
                }
            }
        }

        return auditEntity;
    }

    @Override
    public AE newAudit(final E auditedEntity, final String transactionGuid) {
        if (auditedEntity.isDirty()) {
            throw failuref("Only persisted and non-dirty instances of [%s] can be audited.", auditedEntity.getType().getTypeName());
        }
        // TODO Assert that audited entity is valid?

        final AE audit = new_();
        audit.beginInitialising();

        // properties common to all audit-entities
        audit.setAuditedEntity(auditedEntity);
        audit.setAuditedVersion(auditedEntity.getVersion());
        // Alternatively, annotate AbstractAuditEntity.auditDate with @IsProperty(assignBeforeSave = true)
        audit.setAuditDate(now().toDate());
        // Alternatively, annotate AbstractAuditEntity.auditDate with @IsProperty(assignBeforeSave = true)
        audit.setUser(getUserOrThrow());
        audit.setAuditedTransactionGuid(transactionGuid);

        // specific, audited properties
        final var auditEntityMetadata = domainMetadata.forEntity(getEntityType());
        for (final var auditProp : auditEntityMetadata.properties()) {
            // Audited properties can be identified by their names
            final var auditedPropName = substringAfter(auditProp.name(), A3T + "_");
            if (!auditedPropName.isEmpty()) {
                audit.set(auditProp.name(), auditedEntity.get(auditedPropName));
            }
        }

        audit.endInitialising();
        return audit;
    }

    @Override
    protected IFetchProvider<AE> createFetchProvider() {
        return EntityUtils.fetch(getEntityType())
                .with(domainMetadata.forEntity(getEntityType()).properties().stream()
                              .filter(PropertyMetadata::isPersistent)
                              .map(PropertyMetadata::name)
                              .collect(toSet()));
    }

    /**
     * Returns the current user, if defined; otherwise, throws an exception.
     */
    private User getUserOrThrow() {
        final var user = getUser();
        if (user == null) {
            throw new EntityCompanionException("The current user is not defined.");
        }
        return user;
    }

}
