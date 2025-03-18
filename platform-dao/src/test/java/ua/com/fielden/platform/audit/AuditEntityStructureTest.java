package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.junit.Test;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyNature;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.AUDITED_ENTITY;
import static ua.com.fielden.platform.audit.AuditEntityGenerator.NON_AUDITED_PROPERTIES;
import static ua.com.fielden.platform.audit.AuditUtils.*;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.meta.PropertyNature.*;
import static ua.com.fielden.platform.reflection.Reflector.newParameterizedType;

public class AuditEntityStructureTest extends AbstractDaoTestCase {

    private Class<AbstractAuditEntity<TgVehicle>> tgVehicleAuditType;
    private Class<AbstractSynAuditEntity<TgVehicle>> tgVehicleSynAuditType;
    private Class<AbstractAuditProp<TgVehicle>> tgVehicleAuditPropType;

    @Inject
    void setAuditTypeFinder(final IAuditTypeFinder auditTypeFinder) {
        tgVehicleAuditType = auditTypeFinder.getAuditEntityType(TgVehicle.class);
        tgVehicleSynAuditType = auditTypeFinder.getSynAuditEntityType(TgVehicle.class);
        tgVehicleAuditPropType = auditTypeFinder.getAuditPropTypeForAuditEntity(tgVehicleAuditType);
    }

    @Test
    public void audit_entity_and_audit_prop_types_are_generated_for_TgVehicle() {
        assertTrue(AbstractAuditEntity.class.isAssignableFrom(tgVehicleAuditType));
        assertEquals(TgVehicle.class, getAuditedType(tgVehicleAuditType));

        assertNotNull(tgVehicleAuditPropType);
        assertTrue(AbstractAuditProp.class.isAssignableFrom(tgVehicleAuditPropType));
        assertEquals(TgVehicle.class, getAuditedTypeForAuditPropType(tgVehicleAuditPropType));
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

        final var domainMetadata = getInstance(IDomainMetadata.class);
        final var auditTypeMetadata = domainMetadata.forEntity(tgVehicleAuditType);

        final var persistentPropertiesOfTgVehicle = domainMetadata.forEntity(TgVehicle.class)
                .properties().stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .toList();

        final var expectedProps = ImmutableList.<Prop>builder()
                .add(new Prop(AbstractAuditEntity.AUDITED_VERSION, Long.class, PERSISTENT))
                .add(new Prop(AbstractAuditEntity.AUDIT_DATE, Date.class, PERSISTENT))
                .add(new Prop(AbstractAuditEntity.AUDITED_TRANSACTION_GUID, String.class, PERSISTENT))
                .add(new Prop(AbstractAuditEntity.USER, User.class, PERSISTENT))
                .add(new Prop(KEY, String.class, CALCULATED))
                .add(new Prop(ID, Long.class, PERSISTENT))
                .add(new Prop(VERSION, Long.class, PERSISTENT))
                .add(new Prop(AUDITED_ENTITY, TgVehicle.class, PERSISTENT))
                .addAll(persistentPropertiesOfTgVehicle.stream()
                                .filter(pm -> !NON_AUDITED_PROPERTIES.contains(pm.name()))
                                .map(pm -> new Prop(AuditUtils.auditPropertyName(pm.name()), pm.type().genericJavaType(), PERSISTENT))
                                .toList())
                .build()
                .stream()
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
        final var auditPropTypeMetadata = domainMetadata.forEntity(tgVehicleAuditPropType);

        final var expectedProps = ImmutableList.<Prop>builder()
                .add(new Prop(AbstractAuditProp.AUDIT_ENTITY, tgVehicleAuditType, PERSISTENT))
                .add(new Prop(AbstractAuditProp.PROPERTY, newParameterizedType(PropertyDescriptor.class, tgVehicleSynAuditType), PERSISTENT))
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

}
