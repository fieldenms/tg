package ua.com.fielden.platform.audit;

import com.google.inject.Inject;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowingConsumer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Lazy;

import java.util.stream.Stream;

import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.AUDITED_ENTITY;
import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.AUDITED_VERSION;
import static ua.com.fielden.platform.audit.AuditUtils.auditedPropertyName;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.utils.Lazy.lazySupplier;

/// Extension for AssertJ that provides assertions for audit-entities.
///
final class AuditAssertions {

    private final IDomainMetadata domainMetadata;
    private final ICompanionObjectFinder coFinder;

    @Inject
    AuditAssertions(final IDomainMetadata domainMetadata, final ICompanionObjectFinder coFinder) {
        this.domainMetadata = domainMetadata;
        this.coFinder = coFinder;
    }

    public <E extends AbstractEntity<?>> SynAuditEntityAssert<E> assertThat(AbstractSynAuditEntity<E> a3t) {
        return new SynAuditEntityAssert<>(a3t);
    }

    final class SynAuditEntityAssert<E extends AbstractEntity<?>> extends AbstractAssert<SynAuditEntityAssert<E>, AbstractSynAuditEntity<E>> {

        private final Lazy<ISynAuditEntityDao<E>> lazyCoAudit;

        /// Either the same audit-entity as received in the constructor or a refetched instance.
        /// Refetching is needed if the audit-entity lack properties (i.e. they are proxied) that are necessary for comparison with an audited entity.
        private final Lazy<AbstractSynAuditEntity<E>> lazyRefetchedAudit;

        SynAuditEntityAssert(final AbstractSynAuditEntity<E> a3t) {
            super(a3t, SynAuditEntityAssert.class);
            isNotNull();
            lazyCoAudit = lazySupplier(() -> coFinder.find((Class<AbstractSynAuditEntity<E>>) a3t.getType()));
            lazyRefetchedAudit = lazySupplier(() -> refetchAudit(a3t));
        }

        /// Asserts that the audit-entity under test wholly corresponds to the specified audited entity.
        /// Each active audit-property's value must be equal to that of a corresponding audited property.
        ///
        public SynAuditEntityAssert<E> isAuditFor(final E entity) {
            Assertions.assertThat(entity).isNotNull();

            final var refetchedEntity = refetchAuditedEntity(entity);

            assertPropertyEquals(lazyRefetchedAudit.get(), AUDITED_ENTITY, refetchedEntity);
            assertPropertyEquals(lazyRefetchedAudit.get(), AUDITED_VERSION, refetchedEntity.getVersion());

            domainMetadata.forEntity(lazyRefetchedAudit.get().getType())
                    .properties()
                    .stream()
                    .filter(p -> p.get(AUDIT_PROPERTY).filter(KAuditProperty.Data::active).isPresent())
                    .forEach(p -> isAuditPropertyFor(refetchedEntity, auditedPropertyName(p.name())));

            return this;
        }

        /// Asserts that the audit-entity under test **is not** an audit record for the specified audited entity.
        /// This assertion is true if the specified entity's ID or version is different from that of the audited one.
        /// It is assumed that if ID and version are the same, then the rest of the properties are equal.
        ///
        public SynAuditEntityAssert<E> isNotAuditFor(final E entity) {
            Assertions.assertThat(entity).isNotNull();

            final var refetchedEntity = refetchAuditedEntity(entity);

            if (lazyRefetchedAudit.get().getAuditedVersion().equals(refetchedEntity.getVersion())
                && lazyRefetchedAudit.get().getAuditedEntity().getId().equals(refetchedEntity.getId()))
            {
                failWithMessage("Did not expect audit-entity [%s] to be an audit for [%s] (Version %s)",
                                lazyRefetchedAudit.get(), refetchedEntity, refetchedEntity.getVersion());
            }

            return this;
        }

        /// Asserts that the audited entity's property has a value that is equal to that of a corresponding audit-property in the audit-entity under test.
        ///
        public SynAuditEntityAssert<E> isAuditPropertyFor(final E entity, final CharSequence auditedProperty) {
            assertAuditPropertyEqualsToAudited(lazyRefetchedAudit.get(), entity, auditedProperty);
            return this;
        }

        public SynAuditEntityAssert<E> auditPropertySatisfies(final CharSequence auditedProperty, final ThrowingConsumer<? super Object> cond) {
            Assertions.assertThat(lazyRefetchedAudit.get().<Object>getA3t(auditedProperty))
                    .withFailMessage(() -> "Audited value for [%s] in audit-entity [%s] did not satisfy the given condition."
                                            .formatted(auditedProperty, lazyRefetchedAudit.get()))
                    .satisfies(cond::accept);
            return this;
        }

        private <E extends AbstractEntity<?>> AbstractSynAuditEntity<E> refetchAudit(final AbstractSynAuditEntity<E> a3t) {
            final var proxiedNames = a3t.proxiedPropertyNames();

            final var anyProxied = Stream.concat(
                            Stream.of(AUDITED_ENTITY, AUDITED_VERSION),
                            domainMetadata.forEntity(a3t.getType())
                                    .properties()
                                    .stream()
                                    .filter(p -> p.get(AUDIT_PROPERTY).filter(KAuditProperty.Data::active).isPresent())
                                    .map(PropertyMetadata::name))
                    .anyMatch(proxiedNames::contains);

            // fetchAll is used for simplicity, and is a good approximation.
            return anyProxied ? lazyCoAudit.get().findById(a3t.getId(), (fetch) fetchAll(a3t.getType())) : a3t;
        }

        private E refetchAuditedEntity(final E auditedEntity) {
            final var proxiedNames = auditedEntity.proxiedPropertyNames();

            final var fetchModelForAuditing = lazyCoAudit.get().fetchModelForAuditing();
            final var anyProxied = Stream.concat(fetchModelForAuditing.getIncludedProps().stream(),
                                                 fetchModelForAuditing.getIncludedPropsWithModels().keySet().stream())
                    .anyMatch(proxiedNames::contains);

            return anyProxied
                    ? coFinder.find((Class<E>) auditedEntity.getType()).findById(auditedEntity.getId(), fetchModelForAuditing)
                    : auditedEntity;
        }

        private void assertPropertyEquals(final AbstractEntity<?> entity, final CharSequence property, final Object expected) {
            final var actual = entity.get(property.toString());
            if (!EntityUtils.equalsEx(actual, expected)) {
                failWithActualExpectedAndMessage(
                        actual, expected,
                        "Unexpected value for property [%s] in %s [%s]@[%s].",
                        property,
                        (entity instanceof AbstractSynAuditEntity<?> || entity instanceof AbstractAuditEntity<?>) ? "audit-entity" : "entity",
                        entity,
                        entity.getType().getSimpleName());
            }
        }

        private void assertAuditPropertyEqualsToAudited(
                final AbstractSynAuditEntity<?> auditEntity,
                final AbstractEntity<?> auditedEntity,
                final CharSequence auditedProperty)
        {
            final var actual = auditEntity.getA3t(auditedProperty);
            final var expected = auditedEntity.get(auditedProperty.toString());

            if (!EntityUtils.equalsEx(actual, expected)) {
                failWithActualExpectedAndMessage(
                        actual, expected,
                        "Audit-entity [%s]@[%s] has unexpected value for audited property [%s].",
                        auditEntity,
                        auditEntity.getType().getSimpleName(),
                        auditedProperty);
            }
        }
    }

}
