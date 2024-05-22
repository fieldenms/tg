package ua.com.fielden.platform.meta;

import com.google.inject.Guice;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.meta.Assertions.EntityA;
import ua.com.fielden.platform.meta.PropertyMetadata.Calculated;
import ua.com.fielden.platform.meta.PropertyMetadata.Transient;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.CompositeKey;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Primitive;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test.PlatformTestHibernateSetup;

import java.util.SortedSet;

import static ua.com.fielden.platform.meta.PropertyMetadataKeys.KEY_MEMBER;

public class EntityMetadataTest {

    private final DomainMetadataGenerator generator = new DomainMetadataGenerator(
            Guice.createInjector(new HibernateUserTypesModule()),
            PlatformTestHibernateSetup.getHibernateTypes(),
            DbVersion.MSSQL
    );

    @Test
    public void entity_annotated_with_MapEntityTo_gets_persistent_nature() {
        EntityA.of(generator.forEntity(TgTimesheet.class))
                .assertIs(EntityMetadata.Persistent.class)
                .assertJavaType(TgTimesheet.class);
    }

    @Test
    public void synthetic_entity_gets_synthetic_nature() {
        EntityA.of(generator.forEntity(TgAverageFuelUsage.class))
                .assertIs(EntityMetadata.Synthetic.class)
                .assertJavaType(TgAverageFuelUsage.class);
    }

    @Test
    public void synthetic_entity_based_on_persistent_gets_synthetic_nature() {
        EntityA.of(generator.forEntity(TgReVehicleModel.class))
                .assertIs(EntityMetadata.Synthetic.class)
                .assertJavaType(TgReVehicleModel.class);
    }

    @Test
    public void id_is_generated_as_persistent_for_persistent_entities() {
        EntityA.of(generator.forEntity(TgPerson.class))
                .assertProperty("id", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(Long.class)));
    }

    @Test
    public void id_is_generated_as_persistent_for_synthetic_based_on_persistent_entities() {
        EntityA.of(generator.forEntity(TgReVehicleModel.class))
                .assertProperty("id", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(Long.class)));
    }

    @Test
    public void id_is_generated_as_calculated_for_synthetic_entities_with_entity_typed_key() {
        EntityA.of(generator.forEntity(TgMakeCount.class))
                .assertProperty("id", p -> p
                        .assertIs(Calculated.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(Long.class)));
    }

    @Test
    public void one2one_entity_property_key() {
        EntityA.of(generator.forEntity(TgVehicleTechDetails.class))
                .assertProperty("key", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .assertType(t -> t.assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgVehicle.class)));
    }

    @Test
    public void synthetic_entity_property_key() {
        EntityA.of(generator.forEntity(TgAverageFuelUsage.class))
                .assertProperty("key", p -> p
                        .assertIs(Transient.class)
                        .assertType(t -> t.assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgVehicle.class)));
    }

    @Test
    public void entity_with_DynamicEntityKey() {
        EntityA.of(generator.forEntity(TgOrgUnit2.class))
                .assertProperty("key", p -> p
                        .assertIs(Calculated.class)
                        .assertType(t -> t.assertIs(CompositeKey.class).assertJavaType(String.class)))
                .assertProperty("parent", p -> p.assertKeyEq(KEY_MEMBER, true))
                .assertProperty("name", p -> p.assertKeyEq(KEY_MEMBER, true));
    }

    @Test
    public void version_is_generated_as_persistent_for_persistent_entities() {
        EntityA.of(generator.forEntity(TgPerson.class))
                .assertProperty("version", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(Long.class)));
    }

    @Test
    public void version_is_generated_as_persistent_for_synthetic_based_on_persistent_entities() {
        EntityA.of(generator.forEntity(TgReVehicleModel.class))
                .assertProperty("version", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(Long.class)));
    }

    @Test
    public void metadata_is_generated_for_collectional_properties() {
        EntityA.of(generator.forEntity(TgWagon.class))
                .assertProperty("slots", p -> p
                        .assertIs(PropertyMetadata.Transient.class)
                        .assertType(t -> t.assertCollectional()
                                .assertCollectionType(SortedSet.class)
                                .elementType().assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgWagonSlot.class)));
    }

}
