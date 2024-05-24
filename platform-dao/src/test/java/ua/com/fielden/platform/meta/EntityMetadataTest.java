package ua.com.fielden.platform.meta;

import com.google.inject.Guice;
import org.junit.Test;
import ua.com.fielden.platform.domain.metadata.DomainTreeEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.meta.Assertions.EntityA;
import ua.com.fielden.platform.meta.PropertyMetadata.Calculated;
import ua.com.fielden.platform.meta.PropertyMetadata.CritOnly;
import ua.com.fielden.platform.meta.PropertyMetadata.Transient;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.CompositeKey;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Primitive;
import ua.com.fielden.platform.ref_hierarchy.AbstractTreeEntry;
import ua.com.fielden.platform.ref_hierarchy.TypeLevelHierarchyEntry;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test.PlatformTestHibernateSetup;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.SortedSet;

import static ua.com.fielden.platform.meta.PropertyMetadataKeys.KEY_MEMBER;

public class EntityMetadataTest {

    private final TestDomainMetadataGenerator generator = TestDomainMetadataGenerator.wrap(
            new DomainMetadataGenerator(
                    Guice.createInjector(new HibernateUserTypesModule()),
                    PlatformTestHibernateSetup.getHibernateTypes(),
                    DbVersion.MSSQL));

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

    @Test
    public void metadata_is_generated_for_critOnly_properties() {
        EntityA.of(generator.forEntity(TgWorkOrder.class))
                .assertProperty("intSingle", p -> p
                        .assertIs(CritOnly.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(Integer.class)))
                .assertProperty("boolSingle", p -> p
                        .assertIs(CritOnly.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(boolean.class)))
                .assertProperty("moneySingle", p -> p
                        .assertIs(CritOnly.class)
                        .assertType(t -> t.assertIs(PropertyTypeMetadata.Composite.class).assertJavaType(Money.class)))
                .assertProperty("orgunitCritOnly", p -> p
                        .assertIs(CritOnly.class)
                        .assertType(t -> t.assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgOrgUnit1.class)));
    }

    @Test
    public void metadata_is_generated_for_calculated_properties() {
        EntityA.of(generator.forEntity(TgVehicle.class))
                .assertProperty("lastFuelUsage", p -> p
                        .assertIs(Calculated.class)
                        .assertType(t -> t.assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgFuelUsage.class)))
                .assertProperty("lastFuelUsageQty", p -> p
                        .assertIs(Calculated.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(BigDecimal.class)));
    }

    @Test
    public void metadata_is_generated_for_transient_properties() {
        EntityA.of(generator.forEntity(UnionEntityWithoutSecondDescTitle.class))
                .assertProperty("propertyOne", p -> p
                        .assertIs(Transient.class)
                        .assertType(t -> t.assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(EntityOne.class)))
                .assertProperty("propertyThree", p -> p
                        .assertIs(Transient.class)
                        .assertType(t -> t.assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(EntityThree.class)));
    }

    @Test
    public void metadata_is_not_generated_for_abstract_entity_types() {
        generator.assertNotGenerated(AbstractEntity.class)
                .assertNotGenerated(AbstractTreeEntry.class)
                .assertNotGenerated(AbstractPersistentEntity.class);
    }

    @Test
    public void metadata_is_not_generated_for_non_persistent_functional_entity_types() {
        generator.assertNotGenerated(TgDummyAction.class);
    }

    @Test
    public void metadata_is_not_generated_for_entity_types_with_no_exact_nature() {
        generator.assertNotGenerated(EntityAggregates.class);
        generator.assertNotGenerated(DomainTreeEntity.class);
        generator.assertNotGenerated(TypeLevelHierarchyEntry.class);
    }

}
