package ua.com.fielden.platform.audit;

import com.google.inject.Inject;
import org.assertj.core.api.AbstractAssert;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.utils.EntityUtils;

import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.AUDITED_ENTITY;
import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.AUDITED_VERSION;
import static ua.com.fielden.platform.audit.AuditUtils.auditedPropertyName;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;

/**
 * Extension for AssertJ that provides assertions for audit-entities.
 */
final class AuditAssertions {

    private final IDomainMetadata domainMetadata;

    @Inject
    AuditAssertions(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    public <E extends AbstractEntity<?>> SynAuditEntityAssert<E> assertThat(AbstractSynAuditEntity<E> a3t) {
        return new SynAuditEntityAssert<>(a3t);
    }

    final class SynAuditEntityAssert<E extends AbstractEntity<?>> extends AbstractAssert<SynAuditEntityAssert<E>, AbstractSynAuditEntity<E>> {

        SynAuditEntityAssert(final AbstractSynAuditEntity<E> a3t) {
            super(a3t, SynAuditEntityAssert.class);
            isNotNull();
        }

        /**
         * Asserts that the audit-entity under test wholly corresponds to the specified audited entity.
         * Each active audit-property's value must be equal to that of a corresponding audited property.
         */
        public SynAuditEntityAssert<E> isAuditFor(final E entity) {
            assertPropertyEquals(actual, AUDITED_ENTITY, entity);
            assertPropertyEquals(actual, AUDITED_VERSION, entity.getVersion());

            domainMetadata.forEntity(actual.getType())
                    .properties()
                    .stream()
                    .filter(p -> p.get(AUDIT_PROPERTY).filter(KAuditProperty.Data::active).isPresent())
                    .forEach(p -> isAuditPropertyFor(entity, auditedPropertyName(p.name())));

            return this;
        }

        /**
         * Asserts that the audited entity's property has a value that is equal to that of a corresdponding audit-property in the audit-entity under test.
         */
        public SynAuditEntityAssert<E> isAuditPropertyFor(final E entity, final CharSequence auditedProperty) {
            assertAuditPropertyEqualsToAudited(actual, entity, auditedProperty);
            return this;
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
