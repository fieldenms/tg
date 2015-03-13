package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.hibernate.Hibernate;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;

public class DomainMetadataTest extends BaseEntQueryTCase {
    private final BaseInfoForDomainMetadata baseInfoForDomainMetadata = new BaseInfoForDomainMetadata(DOMAIN_METADATA.getUserMapTo());

    @Test
    public void test_one_to_one_property_metadata() throws Exception {
        final PersistedEntityMetadata<TgVehicle> entityMetadata = DOMAIN_METADATA.generatePersistedEntityMetadata(TgVehicle.class, baseInfoForDomainMetadata);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("finDetails");

        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("finDetails", TgVehicleFinDetails.class, true). //
        hibType(Hibernate.LONG). //
        type(PropertyCategory.EXPRESSION). //
        //expression(expr().prop("id").model()). //
        expression(expr().model(select(TgVehicleFinDetails.class).where().prop("key").eq().extProp("id").model()).model()). //
        build();

        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_that_critonly_props_are_excluded() throws Exception {
        final ModelledEntityMetadata<TgAverageFuelUsage> entityMetadata = DOMAIN_METADATA.generateModelledEntityMetadata(TgAverageFuelUsage.class, baseInfoForDomainMetadata);
        assertNull(entityMetadata.getProps().get("datePeriod"));
    }

    @Test
    public void test_one_to_one_property_metadata_for_synthetic_entity() throws Exception {
        final ModelledEntityMetadata<TgAverageFuelUsage> entityMetadata = DOMAIN_METADATA.generateModelledEntityMetadata(TgAverageFuelUsage.class, baseInfoForDomainMetadata);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("key");
        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("key", TgVehicle.class, false). //
        hibType(Hibernate.LONG). //
        type(PropertyCategory.SYNTHETIC). //
        build();
        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_deduced_id_for_synthetic_entity() throws Exception {
        final ModelledEntityMetadata<TgAverageFuelUsage> entityMetadata = DOMAIN_METADATA.generateModelledEntityMetadata(TgAverageFuelUsage.class, baseInfoForDomainMetadata);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("id");
        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("id", Long.class, false). //
        hibType(Hibernate.LONG). //
        type(PropertyCategory.EXPRESSION). //
        expression(expr().prop("key").model()). //
        build();
        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_deduced_id_for_union_entity() throws Exception {
        final ModelledEntityMetadata<TgBogieLocation> entityMetadata = DOMAIN_METADATA.generateUnionedEntityMetadata(TgBogieLocation.class, baseInfoForDomainMetadata);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("id");

        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("id", Long.class, false). //
        hibType(Hibernate.LONG). //
        type(PropertyCategory.EXPRESSION). //
        expression(expr().caseWhen().prop("wagonSlot").isNotNull().then().prop("wagonSlot.id").when().prop("workshop").isNotNull().then().prop("workshop.id").otherwise().val(null).end().model()). //
        build();
        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_deduced_key_for_union_entity() throws Exception {
        final ModelledEntityMetadata<TgBogieLocation> entityMetadata = DOMAIN_METADATA.generateUnionedEntityMetadata(TgBogieLocation.class, baseInfoForDomainMetadata);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("key");

        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("key", String.class, false). //
        hibType(Hibernate.STRING). //
        type(PropertyCategory.EXPRESSION). //
        expression(expr().caseWhen().prop("wagonSlot").isNotNull().then().prop("wagonSlot.key").when().prop("workshop").isNotNull().then().prop("workshop.key").otherwise().val(null).end().model()). //
        build();
        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_union_entity_generated_models() throws Exception {
        final ModelledEntityMetadata<TgBogieLocation> entityMetadata = DOMAIN_METADATA.generateUnionedEntityMetadata(TgBogieLocation.class, baseInfoForDomainMetadata);

        System.out.println(entityMetadata.getModels());
        assertNull(entityMetadata.getProps().get("datePeriod"));
    }

    @Test
    public void test_props_of_union_entity() throws Exception {
        final PersistedEntityMetadata<TgBogie> bogieEm = DOMAIN_METADATA.generatePersistedEntityMetadata(TgBogie.class, baseInfoForDomainMetadata);

        System.out.println(bogieEm.getProps().get("location"));
        System.out.println(bogieEm.getProps().get("location.workshop"));
        System.out.println(bogieEm.getProps().get("location.wagonSlot"));

        final ModelledEntityMetadata<TgBogieLocation> bogieLocationEm = DOMAIN_METADATA.generateUnionedEntityMetadata(TgBogieLocation.class, baseInfoForDomainMetadata);

        System.out.println(bogieLocationEm.getProps().get("key"));
        System.out.println(bogieLocationEm.getProps().get("workshop"));
        System.out.println(bogieLocationEm.getProps().get("wagonSlot"));
    }
}