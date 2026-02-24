package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.audit.AuditUtils;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings;
import ua.com.fielden.platform.sample.domain.AuditedEntity_a3t_1;
import ua.com.fielden.platform.sample.domain.AuditedEntity_a3t_2;
import ua.com.fielden.platform.sample.domain.ReAuditedEntity_a3t;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;

public class AuditingMetadataTest {

    // TODO: Refactor this test class to use IoC.
    //       Then, use IAuditTypeFinder instead of accessing audit types directly.

    private final TestDomainMetadataGenerator generator;

    public AuditingMetadataTest() {
        final var dbVersionProvider = constantDbVersion(DbVersion.MSSQL);
        generator = TestDomainMetadataGenerator.wrap(new DomainMetadataGenerator(new PlatformHibernateTypeMappings.Provider(dbVersionProvider).get(),
                                                                                 dbVersionProvider));
    }

    @Test
    public void only_audit_properties_have_AUDIT_PROPERTY_key() {
        final var auditedEntity_a3t_1_metadata = generator.forEntity(AuditedEntity_a3t_1.class);

        final var auditPropertyNames = Stream.of("key", "date1", "bool1", "str1")
                .map(AuditUtils::auditPropertyName)
                .collect(toSet());

        assertThat(auditedEntity_a3t_1_metadata.properties())
                .filteredOn(p -> auditPropertyNames.contains(p.name()))
                .allMatch(p -> p.has(AUDIT_PROPERTY));

        assertThat(auditedEntity_a3t_1_metadata.properties())
                .filteredOn(p -> !auditPropertyNames.contains(p.name()))
                .noneMatch(p -> p.has(AUDIT_PROPERTY));
    }

    @Test
    public void active_and_inactive_audit_properties_in_persistent_audit_entity() {
        final var auditedEntity_a3t_1_metadata = generator.forEntity(AuditedEntity_a3t_2.class);

        final var activeAuditPropertyNames = Stream.of("key", "date1", "bool1", "str2")
                .map(AuditUtils::auditPropertyName)
                .collect(toSet());

        final var inactiveAuditPropertyNames = Stream.of("str1")
                .map(AuditUtils::auditPropertyName)
                .collect(toSet());

        assertThat(auditedEntity_a3t_1_metadata.properties())
                .filteredOn(p -> activeAuditPropertyNames.contains(p.name()))
                .allSatisfy(p -> assertThat(p.get(AUDIT_PROPERTY)).hasValueSatisfying(data -> assertThat(data.active()).isTrue()));

        assertThat(auditedEntity_a3t_1_metadata.properties())
                .filteredOn(p -> inactiveAuditPropertyNames.contains(p.name()))
                .allSatisfy(p -> assertThat(p.get(AUDIT_PROPERTY)).hasValueSatisfying(data -> assertThat(data.active()).isFalse()));
    }

    @Test
    public void active_and_inactive_audit_properties_in_synthetic_audit_entity() {
        final var synAuditMetadata = generator.forEntity(ReAuditedEntity_a3t.class);

        final var activeAuditPropertyNames = Stream.of("key", "date1", "bool1", "str2")
                .map(AuditUtils::auditPropertyName)
                .collect(toSet());

        final var inactiveAuditPropertyNames = Stream.of("str1")
                .map(AuditUtils::auditPropertyName)
                .collect(toSet());

        assertThat(synAuditMetadata.properties())
                .filteredOn(p -> activeAuditPropertyNames.contains(p.name()))
                .allSatisfy(p -> assertThat(p.get(AUDIT_PROPERTY)).hasValueSatisfying(data -> assertThat(data.active()).isTrue()));

        assertThat(synAuditMetadata.properties())
                .filteredOn(p -> inactiveAuditPropertyNames.contains(p.name()))
                .allSatisfy(p -> assertThat(p.get(AUDIT_PROPERTY)).hasValueSatisfying(data -> assertThat(data.active()).isFalse()));
    }

}
