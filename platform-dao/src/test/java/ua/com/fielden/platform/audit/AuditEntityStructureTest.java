package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.junit.Test;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataKeys.KAuditProperty;
import ua.com.fielden.platform.meta.PropertyNature;
import ua.com.fielden.platform.sample.domain.AuditedEntity;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.RichText;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.AUDITED_ENTITY;
import static ua.com.fielden.platform.audit.AuditUtils.auditPropertyName;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.meta.PropertyNature.*;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.reflection.Finder.streamProperties;
import static ua.com.fielden.platform.reflection.Reflector.newParameterizedType;

public class AuditEntityStructureTest extends AbstractDaoTestCase {

    private Class<AbstractAuditEntity<AuditedEntity>> auditType;
    private Class<AbstractSynAuditEntity<AuditedEntity>> synAuditType;
    private Class<AbstractAuditProp<AuditedEntity>> auditPropType;
    private IAuditTypeFinder auditTypeFinder;

    @Inject
    void setAuditTypeFinder(final IAuditTypeFinder auditTypeFinder) {
        auditType = auditTypeFinder.navigate(AuditedEntity.class).auditEntityType();
        synAuditType = auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType();
        auditPropType = auditTypeFinder.navigate(AuditedEntity.class).auditPropType();
        this.auditTypeFinder = auditTypeFinder;
    }

    @Test
    public void audit_entity_and_audit_prop_types_are_generated_for_AuditedEntity() {
        assertTrue(AbstractAuditEntity.class.isAssignableFrom(auditType));
        assertEquals(AuditedEntity.class, auditTypeFinder.navigateAudit(auditType).auditedType());

        assertNotNull(auditPropType);
        assertTrue(AbstractAuditProp.class.isAssignableFrom(auditPropType));
        assertEquals(AuditedEntity.class, auditTypeFinder.navigateAuditProp(auditPropType).auditedType());
    }

    @Test
    public void properties_annotated_with_DisableAuditing_are_excluded_from_audit_entities() {
        final var propsWithDisableAuditing = streamProperties(AuditedEntity.class, DisableAuditing.class)
                .map(Field::getName)
                .collect(toSet());
        assertEquals(Set.of(AuditedEntity.Property.str3.toPath()), propsWithDisableAuditing);
        assertFalse(isPropertyPresent(auditType, auditPropertyName(AuditedEntity.Property.str3)));
        assertFalse(isPropertyPresent(synAuditType, auditPropertyName(AuditedEntity.Property.str3)));
    }

    /**
     * This test asserts that the actual structure of properties in a generated audit-entity type matches the expected structure.
     */
    @Test
    public void properties_of_audit_entity_type() {
        record Prop (String name, Type type, PropertyNature nature) {
            @Override
            public String toString() {
                return "%s %s (%s)".formatted(type.getTypeName(), name, nature);
            }
        }

        final var auditTypeMetadata = getInstance(IDomainMetadata.class).forEntity(auditType);

        final var expectedProps = Stream.of(
                        new Prop(AbstractAuditEntity.AUDITED_VERSION, Long.class, PERSISTENT),
                        new Prop(AbstractAuditEntity.AUDIT_DATE, Date.class, PERSISTENT),
                        new Prop(AbstractAuditEntity.AUDITED_TRANSACTION_GUID, String.class, PERSISTENT),
                        new Prop(AbstractAuditEntity.AUDIT_USER, User.class, PERSISTENT),
                        new Prop(KEY, String.class, CALCULATED),
                        new Prop(ID, Long.class, PERSISTENT),
                        new Prop(VERSION, Long.class, PERSISTENT),
                        new Prop(AUDITED_ENTITY, AuditedEntity.class, PERSISTENT),
                        new Prop(auditPropertyName(KEY), String.class, PERSISTENT),
                        new Prop(auditPropertyName(AuditedEntity.Property.date1), Date.class, PERSISTENT),
                        new Prop(auditPropertyName(AuditedEntity.Property.bool1), boolean.class, PERSISTENT),
                        new Prop(auditPropertyName(AuditedEntity.Property.str2), String.class, PERSISTENT),
                        new Prop(auditPropertyName(AuditedEntity.Property.richText), RichText.class, PERSISTENT),
                        new Prop(auditPropertyName(AuditedEntity.Property.union), UnionEntity.class, PERSISTENT),
                        new Prop(auditPropertyName(AuditedEntity.Property.invalidate), boolean.class, PERSISTENT),
                        new Prop(auditPropertyName("str1"), String.class, PLAIN))
                .sorted(Comparator.comparing(Prop::name))
                .toList();

        final var actualProps = auditTypeMetadata.properties().stream()
                .map(p -> new Prop(p.name(), p.type().genericJavaType(), p.nature()))
                .sorted(Comparator.comparing(Prop::name))
                .toList();

        assertEquals(expectedProps, actualProps);
    }

    /**
     * This test asserts that the actual structure of properties in a generated audit-prop entity type matches the expected structure.
     */
    @Test
    public void properties_of_audit_prop_entity_type() {
        record Prop (String name, Type type, PropertyNature nature) {
            @Override
            public String toString() {
                return "%s %s (%s)".formatted(type.getTypeName(), name, nature);
            }
        }

        final var domainMetadata = getInstance(IDomainMetadata.class);
        final var auditPropTypeMetadata = domainMetadata.forEntity(auditPropType);

        final var expectedProps = ImmutableList.<Prop>builder()
                .add(new Prop(AbstractAuditProp.AUDIT_ENTITY, auditType, PERSISTENT))
                .add(new Prop(AbstractAuditProp.PROPERTY, newParameterizedType(PropertyDescriptor.class, synAuditType), PERSISTENT))
                .add(new Prop(KEY, String.class, CALCULATED))
                .add(new Prop(ID, Long.class, PERSISTENT))
                .add(new Prop(VERSION, Long.class, PERSISTENT))
                .build()
                .stream()
                .sorted(Comparator.comparing(Prop::name))
                .toList();

        final var actualProps = auditPropTypeMetadata.properties().stream()
                .map(p -> new Prop(p.name(), p.type().genericJavaType(), p.nature()))
                .sorted(Comparator.comparing(Prop::name))
                .toList();

        assertEquals(expectedProps, actualProps);
    }

    @Test
    public void naming_of_columns_for_audit_properties() {
        final var domainMetadata = getInstance(IDomainMetadata.class);

        final var auditType = auditTypeFinder.navigate(AuditedEntity.class).auditEntityType();
        domainMetadata.forEntity(auditType)
                .properties()
                .stream()
                .filter(p -> p.get(AUDIT_PROPERTY).filter(KAuditProperty.Data::active).isPresent())
                .forEach(auditProp -> {
                    final var persistentAuditProp = assertThat(auditProp.asPersistent()).isPresent().get().actual();
                    final var auditedProp = domainMetadata.forProperty(AuditedEntity.class, AuditUtils.auditedPropertyName(auditProp.name()));
                    final var persistentAuditedProp = assertThat(auditedProp.asPersistent()).isPresent().get().actual();
                    assertEquals("A3T_" + persistentAuditedProp.data().column().name, persistentAuditProp.data().column().name);
                });
    }

}
