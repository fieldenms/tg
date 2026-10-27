package ua.com.fielden.platform.web.audit;

import org.junit.Test;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.AuditedEntity;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.*;
import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.CHANGED_PROPS;
import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.CHANGED_PROPS_CRIT;
import static ua.com.fielden.platform.audit.AuditUtils.auditPropertyName;
import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;

/// Tests for [AuditWebUiConfigFactory].
///
public class AuditWebUiConfigFactoryTest extends AbstractWebResourceWithDaoTestCase {

    private final AuditWebUiConfigFactory auditWebUiConfigFactory = getInstance(AuditWebUiConfigFactory.class);
    private final IAuditTypeFinder auditTypeFinder = getInstance(IAuditTypeFinder.class);
    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);

    @Test
    public void audit_entity_centre_criteria_for_audited_entity() {
        final var synAuditEntityType = auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType();
        final var ecc = auditWebUiConfigFactory.createCentreConfig(AuditedEntity.class, synAuditEntityType);
        assertThat(assertPresent(ecc.getSelectionCriteria()))
                .containsExactlyInAnyOrder(
                        AUDITED_ENTITY,
                        AUDITED_VERSION,
                        AUDIT_DATE,
                        AUDIT_USER,
                        CHANGED_PROPS_CRIT,
                        auditPropertyName(AuditedEntity.Property.key),
                        auditPropertyName(AuditedEntity.Property.date1),
                        auditPropertyName(AuditedEntity.Property.date2),
                        auditPropertyName(AuditedEntity.Property.bool1),
                        auditPropertyName(AuditedEntity.Property.str1),
                        auditPropertyName(AuditedEntity.Property.str2),
                        auditPropertyName(AuditedEntity.Property.richText),
                        auditPropertyName(AuditedEntity.Property.union),
                        auditPropertyName(AuditedEntity.Property.invalidate));
    }

    @Test
    public void audit_entity_centre_resultset_properties_for_audited_entity() {
        final var synAuditEntityType = auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType();
        final var ecc = auditWebUiConfigFactory.createCentreConfig(AuditedEntity.class, synAuditEntityType);
        assertThat(assertPresent(ecc.getResultSetProperties()))
                .extracting(prop -> prop.propName.orElse(""))
                .containsExactlyInAnyOrder(
                        AUDITED_ENTITY,
                        AUDITED_VERSION,
                        AUDIT_DATE,
                        AUDIT_USER,
                        CHANGED_PROPS,
                        auditPropertyName(AuditedEntity.Property.key),
                        auditPropertyName(AuditedEntity.Property.date1),
                        auditPropertyName(AuditedEntity.Property.date2),
                        auditPropertyName(AuditedEntity.Property.bool1),
                        auditPropertyName(AuditedEntity.Property.str1),
                        auditPropertyName(AuditedEntity.Property.str2),
                        auditPropertyName(AuditedEntity.Property.richText),
                        auditPropertyName(AuditedEntity.Property.union),
                        auditPropertyName(AuditedEntity.Property.invalidate),
                        auditPropertyName(AuditedEntity.Property.colour),
                        auditPropertyName(AuditedEntity.Property.hyperlink));
    }

    @Test
    public void audit_entity_centre_for_audited_entity_does_not_contain_criteria_for_colour() {
        assertAuditedPropertyNotInCentreCrit(AuditedEntity.class, "colour", Colour.class);
    }

    @Test
    public void audit_entity_centre_for_audited_entity_does_not_contain_criteria_for_hyperlink() {
        assertAuditedPropertyNotInCentreCrit(AuditedEntity.class, "hyperlink", Hyperlink.class);
    }

    @Test
    public void audit_entity_embedded_centre_criteria_for_audited_entity() {
        final var synAuditEntityType = auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType();
        final var ecc = auditWebUiConfigFactory.createEmbeddedCentreConfig(synAuditEntityType);
        assertThat(assertPresent(ecc.getSelectionCriteria()))
                .containsExactlyInAnyOrder(
                        AUDITED_VERSION,
                        AUDIT_DATE,
                        AUDIT_USER,
                        CHANGED_PROPS_CRIT,
                        auditPropertyName(AuditedEntity.Property.key),
                        auditPropertyName(AuditedEntity.Property.date1),
                        auditPropertyName(AuditedEntity.Property.date2),
                        auditPropertyName(AuditedEntity.Property.bool1),
                        auditPropertyName(AuditedEntity.Property.str1),
                        auditPropertyName(AuditedEntity.Property.str2),
                        auditPropertyName(AuditedEntity.Property.richText),
                        auditPropertyName(AuditedEntity.Property.union),
                        auditPropertyName(AuditedEntity.Property.invalidate));
    }


    @Test
    public void audit_entity_embedded_centre_resultset_properties_for_audited_entity() {
        final var synAuditEntityType = auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType();
        final var ecc = auditWebUiConfigFactory.createEmbeddedCentreConfig(synAuditEntityType);
        assertThat(assertPresent(ecc.getResultSetProperties()))
                .extracting(prop -> prop.propName.orElse(""))
                .containsExactlyInAnyOrder(
                        AUDITED_VERSION,
                        AUDIT_DATE,
                        AUDIT_USER,
                        CHANGED_PROPS,
                        auditPropertyName(AuditedEntity.Property.key),
                        auditPropertyName(AuditedEntity.Property.date1),
                        auditPropertyName(AuditedEntity.Property.date2),
                        auditPropertyName(AuditedEntity.Property.bool1),
                        auditPropertyName(AuditedEntity.Property.str1),
                        auditPropertyName(AuditedEntity.Property.str2),
                        auditPropertyName(AuditedEntity.Property.richText),
                        auditPropertyName(AuditedEntity.Property.union),
                        auditPropertyName(AuditedEntity.Property.invalidate),
                        auditPropertyName(AuditedEntity.Property.colour),
                        auditPropertyName(AuditedEntity.Property.hyperlink));
    }

    @Test
    public void audit_entity_embedded_centre_for_audited_entity_does_not_contain_criteria_for_colour() {
        assertAuditedPropertyNotInEmbeddedCentreCrit(AuditedEntity.class, AuditedEntity.Property.colour, Colour.class);
    }

    @Test
    public void audit_entity_embedded_centre_for_audited_entity_does_not_contain_criteria_for_hyperlink() {
        assertAuditedPropertyNotInEmbeddedCentreCrit(AuditedEntity.class, AuditedEntity.Property.hyperlink, Hyperlink.class);
    }

    private void assertAuditedPropertyNotInCentreCrit(
            final Class<? extends AbstractEntity<?>> auditedType,
            final CharSequence auditedProp,
            final Class<?> auditedPropType)
    {
        final var synAuditEntityType = auditTypeFinder.navigate(auditedType).synAuditEntityType();
        final var auditedPropMetadata = assertPresent(domainMetadata.forPropertyOpt(auditedType, auditedProp));
        assertEquals(auditedPropType, auditedPropMetadata.type().javaType());
        final var auditPropMetadata = assertPresent(domainMetadata.forPropertyOpt(synAuditEntityType, auditPropertyName(auditedPropMetadata.name())));
        assertEquals(auditedPropType, auditPropMetadata.type().javaType());

        final var ecc = auditWebUiConfigFactory.createCentreConfig(auditedType, synAuditEntityType);
        assertThat(assertPresent(ecc.getSelectionCriteria()))
                .doesNotContain(auditPropMetadata.name());
    }

    private void assertAuditedPropertyNotInEmbeddedCentreCrit(
            final Class<? extends AbstractEntity<?>> auditedType,
            final CharSequence auditedProp,
            final Class<?> auditedPropType)
    {
        final var synAuditEntityType = auditTypeFinder.navigate(auditedType).synAuditEntityType();
        final var auditedPropMetadata = assertPresent(domainMetadata.forPropertyOpt(auditedType, auditedProp));
        assertEquals(auditedPropType, auditedPropMetadata.type().javaType());
        final var auditPropMetadata = assertPresent(domainMetadata.forPropertyOpt(synAuditEntityType, auditPropertyName(auditedPropMetadata.name())));
        assertEquals(auditedPropType, auditPropMetadata.type().javaType());

        final var ecc = auditWebUiConfigFactory.createEmbeddedCentreConfig(synAuditEntityType);
        assertThat(assertPresent(ecc.getSelectionCriteria()))
                .doesNotContain(auditPropMetadata.name());
    }

}