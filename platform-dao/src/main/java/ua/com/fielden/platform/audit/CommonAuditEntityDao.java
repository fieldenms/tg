package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableBiMap;
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
    /**
     * A bidirectional mapping between names of audited and audit properties.
     * <p>
     * Standard direction: keys - audited properties, values - audit properties.
     * <p>
     * Inverse direction: keys - audit properties, values - audited properties.
     * <p>
     * This field is effectively final, but cannot be declared so due to late initialisation; it depends on {@link #domainMetadata}
     * which is provided via method injection.
     */
    private ImmutableBiMap<String, String> auditedToAuditPropertyNames;

    protected CommonAuditEntityDao() {
        super();
        auditPropType = getAuditPropTypeForAuditType(getEntityType());
    }

    @Inject
    protected void setDomainMetadata(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
        this.auditedToAuditPropertyNames = makeAuditedToAuditPropertyNames(domainMetadata);
    }

    /**
     * Returns the name of a property of this audit-entity type that audits the specified property of the audited entity type,
     * if the specified property is indeed audited; otherwise, returns {@code null}.
     */
    protected final @Nullable String getAuditPropertyName(final CharSequence auditedProperty) {
        return auditedToAuditPropertyNames.get(auditedProperty.toString());
    }

    /**
     * Returns the name of a property of the audited entity type that is audited by the specified property of this audit-entity type,
     * if the specified property is an audit property; otherwise, returns {@code null}.
     */
    protected final @Nullable String getAuditedPropertyName(final CharSequence auditProperty) {
        return auditedToAuditPropertyNames.inverse().get(auditProperty.toString());
    }

    @Override
    public AE audit(final E auditedEntity, final String transactionGuid, final Iterable<? extends CharSequence> dirtyProperties) {
        final AE auditEntity = save(newAudit(auditedEntity, transactionGuid));

        if (!Iterables.isEmpty(dirtyProperties)) {
            // Audit information about changed properites
            final IAuditPropDao<AE, AbstractAuditProp<AE>> coAuditProp = co(auditPropType);
            final boolean isNewAuditedEntity = auditedEntity.getVersion() == 0L;
            for (final var property : dirtyProperties) {
                final var auditProperty = getAuditPropertyName(property.toString());
                // Ignore properties that are not audited.
                // Ignore nulls if this is the very first version of the audited entity, which means that there are no historical values for its properties.
                if (auditProperty != null && !(isNewAuditedEntity && auditedEntity.get(property.toString()) == null)) {
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
        // Alternatively, annotate AbstractAuditEntity.user with @IsProperty(assignBeforeSave = true)
        audit.setUser(getUserOrThrow());
        audit.setAuditedTransactionGuid(transactionGuid);

        // specific, audited properties
        auditedToAuditPropertyNames.forEach((auditedProp, auditProp) -> audit.set(auditProp, auditedEntity.get(auditedProp)));

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

    private ImmutableBiMap<String, String> makeAuditedToAuditPropertyNames(final IDomainMetadata domainMetadata) {
        final var auditEntityMetadata = domainMetadata.forEntity(getEntityType());
        final var builder = ImmutableBiMap.<String, String> builderWithExpectedSize(auditEntityMetadata.properties().size() - 6);
        for (final PropertyMetadata property : auditEntityMetadata.properties()) {
            final var auditedPropName = AuditUtils.auditedPropertyName(property.name());
            if (auditedPropName != null) {
                builder.put(auditedPropName, property.name());
            }
        }
        return builder.buildOrThrow();
    }

}
