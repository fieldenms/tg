package ua.com.fielden.platform.audit;

import org.junit.Test;
import ua.com.fielden.platform.audit.IAuditEntityGenerator.SourceInfo;
import ua.com.fielden.platform.audit.IAuditEntityGenerator.VersionStrategy;
import ua.com.fielden.platform.audit.annotations.Audited;
import ua.com.fielden.platform.audit.annotations.DisableAuditing;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.AuditedEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.audit.AuditUtils.auditPropertyName;
import static ua.com.fielden.platform.audit.AuditUtils.getAuditTypeVersion;

/// Tests for the generation of audit-properties with respect to annotation `@Invisible`.
///
/// Each test generates audit types for a variant of [AuditedEntity]: a subtype that redeclares some of its properties.
/// A variant stands for [AuditedEntity] after a change to those properties, with the audit types of [AuditedEntity] as the audit types generated before the change.
/// Hence, the generated sources are those of the next version of the audit-entity type and of the synthetic audit-entity type.
///
public class AuditEntityGeneratorInvisiblePropertiesTest extends AbstractDaoTestCase {

    private final IAuditTypeFinder auditTypeFinder = getInstance(IAuditTypeFinder.class);
    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);

    @Test
    public void no_audit_properties_are_declared_if_audited_properties_have_not_changed() {
        final var sources = generate(NoChange.class);

        assertThat(sources.auditEntity).doesNotContain(" a3t_");
        // The synthetic audit-entity type is as the one that was generated for the audited entity.
        assertThat(declaration(sources.synAuditEntity, AuditedEntity.Property.secret)).contains("@Invisible").doesNotContain("centreOnly");
        assertThat(declaration(sources.synAuditEntity, AuditedEntity.Property.date1)).contains("@Invisible").contains("centreOnly = true");
        assertThat(declaration(sources.synAuditEntity, AuditedEntity.Property.str2)).doesNotContain("@Invisible");
        assertThat(declaration(sources.synAuditEntity, AuditedEntity.Property.token)).contains("@Invisible", "@InactiveAuditProperty");
    }

    @Test
    public void active_audit_property_is_declared_anew_with_Invisible_if_its_audited_property_became_invisible() {
        final var sources = generate(Str2BecameInvisible.class);

        assertThat(declaration(sources.auditEntity, AuditedEntity.Property.str2))
                .contains("@Invisible", "@MapTo(\"A3T_STR2\")", "@Final")
                .doesNotContain("centreOnly", "@InactiveAuditProperty");
        assertThat(declaration(sources.synAuditEntity, AuditedEntity.Property.str2))
                .contains("@Invisible")
                .doesNotContain("centreOnly", "@InactiveAuditProperty");
        assertOnlyDeclared(sources, AuditedEntity.Property.str2);
    }

    @Test
    public void active_audit_property_is_declared_anew_without_Invisible_if_its_audited_property_is_no_longer_invisible() {
        final var sources = generate(SecretBecameVisible.class);

        assertThat(declaration(sources.auditEntity, AuditedEntity.Property.secret))
                .contains("@MapTo", "@Final")
                .doesNotContain("@Invisible", "@InactiveAuditProperty");
        assertThat(declaration(sources.synAuditEntity, AuditedEntity.Property.secret))
                .doesNotContain("@Invisible");
        assertOnlyDeclared(sources, AuditedEntity.Property.secret);
    }

    @Test
    public void active_audit_property_is_declared_anew_if_centreOnly_of_Invisible_changed() {
        final var sources = generate(Date1BecameInvisible.class);

        assertThat(declaration(sources.auditEntity, AuditedEntity.Property.date1))
                .contains("@Invisible", "@MapTo")
                .doesNotContain("centreOnly");
        assertThat(declaration(sources.synAuditEntity, AuditedEntity.Property.date1))
                .contains("@Invisible")
                .doesNotContain("centreOnly");
        assertOnlyDeclared(sources, AuditedEntity.Property.date1);
    }

    /// The audit-property of `str1` is inactive, and is not invisible.
    /// Here `str1` is a property whose auditing is disabled, and which is annotated with `@Invisible` afterwards.
    ///
    @Test
    public void inactive_audit_property_is_declared_anew_with_Invisible_if_its_audited_property_became_invisible() {
        final var sources = generate(Str1WithDisabledAuditingBecameInvisible.class);

        assertThat(declaration(sources.auditEntity, AuditedEntity.Property.str1))
                .contains("@Invisible", "@InactiveAuditProperty", "value = \"Str 1 [removed]\"", "desc = \"[Str 1] at the time of the audited event.\"")
                .doesNotContain("@MapTo", "[removed] [removed]");
        assertThat(declaration(sources.synAuditEntity, AuditedEntity.Property.str1))
                .contains("@Invisible", "@InactiveAuditProperty");
        assertOnlyDeclared(sources, AuditedEntity.Property.str1);
    }

    /// The audit-property of `note` is inactive and invisible.
    ///
    @Test
    public void inactive_audit_property_is_declared_anew_without_Invisible_if_its_audited_property_is_no_longer_invisible() {
        final var sources = generate(NoteWithDisabledAuditingBecameVisible.class);

        assertThat(declaration(sources.auditEntity, AuditedEntity.Property.note))
                .contains("@InactiveAuditProperty")
                .doesNotContain("@Invisible", "@MapTo", "[removed] [removed]");
        assertThat(declaration(sources.synAuditEntity, AuditedEntity.Property.note))
                .contains("@InactiveAuditProperty")
                .doesNotContain("@Invisible");
        assertOnlyDeclared(sources, AuditedEntity.Property.note);
    }

    @Test
    public void audit_property_that_becomes_inactive_has_Invisible_of_its_audited_property_if_that_is_still_declared() {
        // The audit-property of `str2` is not invisible.
        final var sourcesForStr2 = generate(Str2BecameInvisibleAndItsAuditingDisabled.class);
        assertThat(declaration(sourcesForStr2.auditEntity, AuditedEntity.Property.str2))
                .contains("@Invisible", "@InactiveAuditProperty")
                .doesNotContain("@MapTo");
        assertThat(declaration(sourcesForStr2.synAuditEntity, AuditedEntity.Property.str2))
                .contains("@Invisible", "@InactiveAuditProperty");
        assertOnlyDeclared(sourcesForStr2, AuditedEntity.Property.str2);

        // The audit-property of `secret` is invisible.
        final var sourcesForSecret = generate(SecretBecameVisibleAndItsAuditingDisabled.class);
        assertThat(declaration(sourcesForSecret.auditEntity, AuditedEntity.Property.secret))
                .contains("@InactiveAuditProperty")
                .doesNotContain("@Invisible", "@MapTo");
        assertThat(declaration(sourcesForSecret.synAuditEntity, AuditedEntity.Property.secret))
                .contains("@InactiveAuditProperty")
                .doesNotContain("@Invisible");
        assertOnlyDeclared(sourcesForSecret, AuditedEntity.Property.secret);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Variants of AuditedEntity
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class NoChange extends AuditedEntity {}

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class Str2BecameInvisible extends AuditedEntity {
        @IsProperty
        @MapTo("STR2")
        @Invisible
        private String str2;
    }

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class SecretBecameVisible extends AuditedEntity {
        @IsProperty
        @MapTo
        private String secret;
    }

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class Date1BecameInvisible extends AuditedEntity {
        @IsProperty
        @MapTo
        @Invisible
        private Date date1;
    }

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class Str1WithDisabledAuditingBecameInvisible extends AuditedEntity {
        @IsProperty
        @MapTo
        @DisableAuditing
        @Invisible
        private String str1;
    }

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class NoteWithDisabledAuditingBecameVisible extends AuditedEntity {
        @IsProperty
        @MapTo
        @DisableAuditing
        private String note;
    }

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class Str2BecameInvisibleAndItsAuditingDisabled extends AuditedEntity {
        @IsProperty
        @MapTo("STR2")
        @DisableAuditing
        @Invisible
        private String str2;
    }

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class SecretBecameVisibleAndItsAuditingDisabled extends AuditedEntity {
        @IsProperty
        @MapTo
        @DisableAuditing
        private String secret;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /// The sources of the audit-entity type and of the synthetic audit-entity type, generated for a variant of [AuditedEntity].
    ///
    private record Sources (String auditEntity, String synAuditEntity) {}

    /// Generates a new version of audit types for `variant`, with the audit types of [AuditedEntity] as its existing audit types.
    ///
    private Sources generate(final Class<? extends AuditedEntity> variant) {
        final var variantAuditTypeFinder = (IAuditTypeFinder) Proxy.newProxyInstance(
                IAuditTypeFinder.class.getClassLoader(),
                new Class<?>[] {IAuditTypeFinder.class},
                (proxy, method, args) -> method.invoke(auditTypeFinder, "navigate".equals(method.getName()) ? new Object[] {AuditedEntity.class} : args));
        final var generator = new AuditEntityGenerator(AuditingMode.ENABLED, domainMetadata, variantAuditTypeFinder);

        final var sources = generator.generateSources(List.of(variant), VersionStrategy.NEW);

        final var nextVersion = getAuditTypeVersion(auditTypeFinder.navigate(AuditedEntity.class).auditEntityType()) + 1;
        return new Sources(sourceOf(sources, "%s_a3t_%s".formatted(variant.getSimpleName(), nextVersion)),
                           sourceOf(sources, "Re%s_a3t".formatted(variant.getSimpleName())));
    }

    private static String sourceOf(final Collection<SourceInfo> sources, final String simpleName) {
        final var matching = sources.stream().filter(info -> info.className().endsWith("." + simpleName)).toList();
        assertEquals("Sources of [%s] among %s.".formatted(simpleName, sources.stream().map(SourceInfo::className).toList()),
                     1, matching.size());
        return matching.getFirst().source();
    }

    /// Returns the declaration of the audit-property for `auditedProperty` in `source`: its field with annotations.
    /// It is an error if there is no such declaration.
    ///
    private static String declaration(final String source, final CharSequence auditedProperty) {
        final var matcher = Pattern.compile("(?:\\n|\\{)\\n((?:  [^\\n]*\\n)*?  private [^\\n]* %s;)".formatted(Pattern.quote(auditPropertyName(auditedProperty))))
                .matcher(source);
        assertThat(matcher.find()).as("Declaration of the audit-property for [%s] in:%n%s", auditedProperty, source).isTrue();
        return matcher.group(1);
    }

    /// Asserts that the generated audit-entity type declares the audit-property for `auditedProperty`, and no other.
    ///
    private static void assertOnlyDeclared(final Sources sources, final CharSequence auditedProperty) {
        final var matcher = Pattern.compile("  private [^\\n]* (a3t_\\w+);").matcher(sources.auditEntity);
        assertThat(matcher.results().map(result -> result.group(1)))
                .containsExactly(auditPropertyName(auditedProperty));
    }

}
