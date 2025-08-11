package ua.com.fielden.platform.audit;

import jakarta.inject.Inject;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.utils.EntityUtils;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.AUDITED_ENTITY;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.AUDITED_VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Base type for implementations of synthetic audit-entity companion objects.
 * <p>
 * Cannot be used if auditing is disabled. Will throw {@link AuditingModeException} upon construction.
 *
 * @param <E>  the audited entity type
 */
public abstract class CommonSynAuditEntityDao<E extends AbstractEntity<?>>
        extends CommonEntityDao<AbstractSynAuditEntity<E>>
        implements ISynAuditEntityDao<E>
{

    private static final String ERR_AUDIT_RECORD_DOES_NOT_EXIST = "Audit record does not exist for entity [%s] with ID=%s, version=%s.";

    // All fields below are effectively final, but cannot be declared so due to late initialisation.

    private IDomainMetadata domainMetadata;
    private IEntityAuditor<E> coAuditEntity;
    private Class<E> auditedType;

    @Inject
    protected void init(
            final AuditingMode auditingMode,
            final IAuditTypeFinder a3tFinder,
            final ICompanionObjectFinder coFinder,
            final IDomainMetadata domainMetadata)
    {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(this.getClass(), auditingMode);
        }

        this.domainMetadata = domainMetadata;
        final var navigator = a3tFinder.navigateSynAudit(getEntityType());
        coAuditEntity = coFinder.find(navigator.auditEntityType());
        auditedType = navigator.auditedType();
    }

    @Override
    public void audit(final E auditedEntity, final String transactionGuid, final Collection<String> dirtyProperties) {
        coAuditEntity.audit(auditedEntity, transactionGuid, dirtyProperties);
    }

    @Override
    @SessionRequired
    public Stream<AbstractSynAuditEntity<E>> streamAudits(final Long auditedEntityId, @Nullable final fetch<AbstractSynAuditEntity<E>> fetchModel) {
        final var query = select(getEntityType())
                .where()
                .prop(AUDITED_ENTITY).eq().val(auditedEntityId)
                .model();
        return stream(from(query).with(fetchModel).model());
    }

    @Override
    @SessionRequired
    public Stream<AbstractSynAuditEntity<E>> streamAudits(final Long auditedEntityId, final int fetchSize, @Nullable final fetch<AbstractSynAuditEntity<E>> fetchModel) {
        final var query = select(getEntityType())
                .where()
                .prop(AUDITED_ENTITY).eq().val(auditedEntityId)
                .model();
        return stream(from(query).with(fetchModel).model(), fetchSize);
    }

    @Override
    @SessionRequired
    public List<AbstractSynAuditEntity<E>> getAudits(final Long auditedEntityId, @Nullable final fetch<AbstractSynAuditEntity<E>> fetchModel) {
        final var query = select(getEntityType())
                .where()
                .prop(AUDITED_ENTITY).eq().val(auditedEntityId)
                .model();
        return getAllEntities(from(query).with(fetchModel).model());
    }

    @Override
    @SessionRequired
    public @Nullable AbstractSynAuditEntity<E> getAudit(final Long auditedEntityId, final Long version, @Nullable final fetch<AbstractSynAuditEntity<E>> fetchModel) {
        final var query = select(getEntityType())
                .where()
                    .prop(AUDITED_ENTITY).eq().val(auditedEntityId)
                    .and()
                    .prop(AUDITED_VERSION).eq().val(version)
                .model();
        return getEntity(from(query).with(fetchModel).model());
    }

    @Override
    public AbstractSynAuditEntity<E> getAuditOrThrow(
            final Long auditedEntityId,
            final Long version,
            @Nullable final fetch<AbstractSynAuditEntity<E>> fetchModel)
    {
        final var audit = getAudit(auditedEntityId, version, fetchModel);
        if (audit == null) {
            throw new EntityCompanionException(ERR_AUDIT_RECORD_DOES_NOT_EXIST.formatted(auditedType.getSimpleName(), auditedEntityId, version));
        }
        return audit;
    }

    @Override
    protected IFetchProvider<AbstractSynAuditEntity<E>> createFetchProvider() {
        return EntityUtils.fetch(getEntityType())
                .with(domainMetadata.forEntity(getEntityType()).properties().stream()
                              .map(PropertyMetadata::name)
                              .collect(toSet()));
    }

    @Override
    public fetch<E> fetchModelForAuditing() {
        return coAuditEntity.fetchModelForAuditing();
    }

}
