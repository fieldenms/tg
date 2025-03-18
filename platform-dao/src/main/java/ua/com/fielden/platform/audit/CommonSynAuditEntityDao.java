package ua.com.fielden.platform.audit;

import jakarta.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.utils.EntityUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.AUDITED_ENTITY;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.AUDITED_VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Base type for implementations of synthetic audit-entity companion objects.
 *
 * @param <E>  the audited entity type
 */
public abstract class CommonSynAuditEntityDao<E extends AbstractEntity<?>>
        extends CommonEntityDao<AbstractSynAuditEntity<E>>
        implements ISynAuditEntityDao<E>
{

    // All fields below are effectively final, but cannot be declared so due to late initialisation.

    private IDomainMetadata domainMetadata;
    private IEntityAuditor<E> coAuditEntity;

    @Inject
    protected void init(
            final IAuditTypeFinder a3tFinder,
            final ICompanionObjectFinder coFinder,
            final IDomainMetadata domainMetadata)
    {
        this.domainMetadata = domainMetadata;
        coAuditEntity = coFinder.find(a3tFinder.getAuditEntityType(AuditUtils.getAuditedTypeForSyn(getEntityType())));
    }

    @Override
    public void audit(final E auditedEntity, final String transactionGuid, final Iterable<? extends CharSequence> dirtyProperties) {
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
    protected IFetchProvider<AbstractSynAuditEntity<E>> createFetchProvider() {
        return EntityUtils.fetch(getEntityType())
                .with(domainMetadata.forEntity(getEntityType()).properties().stream()
                              .map(PropertyMetadata::name)
                              .collect(toSet()));
    }

}
