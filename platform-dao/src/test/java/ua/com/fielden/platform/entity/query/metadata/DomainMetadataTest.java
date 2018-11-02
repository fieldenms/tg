package ua.com.fielden.platform.entity.query.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.UNION_ENTITY_DETAILS;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.UNION_ENTITY_HEADER;

import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;

public class DomainMetadataTest extends BaseEntQueryTCase {
    private static final BaseInfoForDomainMetadata baseInfoForDomainMetadata = new BaseInfoForDomainMetadata();

    private static <ET extends AbstractEntity<?>> PersistedEntityMetadata<ET> ppp(Class<ET> type) throws Exception {
        return DOMAIN_METADATA.generatePersistedEntityMetadata(baseInfoForDomainMetadata.getEntityTypeInfo(type));
    }
    
    private static <ET extends AbstractEntity<?>> ModelledEntityMetadata<ET> mmm(Class<ET> type) throws Exception {
        return DOMAIN_METADATA.generateModelledEntityMetadata(baseInfoForDomainMetadata.getEntityTypeInfo(type));
    }
    
    private static <ET extends AbstractEntity<?>> ModelledEntityMetadata<ET> uuu(Class<ET> type) throws Exception {
        return DOMAIN_METADATA.generateUnionedEntityMetadata(baseInfoForDomainMetadata.getEntityTypeInfo(type));
    }
    
    private static <ET extends AbstractEntity<?>> EntityTypeInfo<ET> pi(Class<ET> entityType) {
        return baseInfoForDomainMetadata.getEntityTypeInfo(entityType);
    }

    @Test
    public void test_calc_property_of_entity_type_metadata() throws Exception {
        final PersistedEntityMetadata<TgVehicle> entityMetadata = ppp(VEHICLE);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("lastFuelUsage");

        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("lastFuelUsage", TgFuelUsage.class, true, pi(VEHICLE)). //
        hibType(LongType.INSTANCE). //
        category(PropertyCategory.EXPRESSION). //
        expression(expr().model(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).model()).model()). //
        build();

        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    
    @Test
    public void test_one_to_one_property_metadata() throws Exception {
        final PersistedEntityMetadata<TgVehicle> entityMetadata = ppp(VEHICLE);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("finDetails");

        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("finDetails", TgVehicleFinDetails.class, true, pi(VEHICLE)). //
        hibType(LongType.INSTANCE). //
        category(PropertyCategory.EXPRESSION). //
        //expression(expr().prop("id").model()). //
        expression(expr().model(select(TgVehicleFinDetails.class).where().prop("key").eq().extProp("id").model()).model()). //
        build();

        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_that_critonly_props_are_excluded() throws Exception {
        final ModelledEntityMetadata<TgAverageFuelUsage> entityMetadata = mmm(TgAverageFuelUsage.class);
        assertNull(entityMetadata.getProps().get("datePeriod"));
    }

    @Test
    public void test_one_to_one_property_metadata_for_synthetic_entity() throws Exception {
        final ModelledEntityMetadata<TgAverageFuelUsage> entityMetadata = mmm(AVERAGE_FUEL_USAGE);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("key");
        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("key", TgVehicle.class, false, pi(AVERAGE_FUEL_USAGE)). //
        hibType(LongType.INSTANCE). //
        category(PropertyCategory.SYNTHETIC). //
        build();
        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_deduced_id_for_synthetic_entity() throws Exception {
        final ModelledEntityMetadata<TgAverageFuelUsage> entityMetadata = mmm(AVERAGE_FUEL_USAGE);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("id");
        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("id", Long.class, false, pi(AVERAGE_FUEL_USAGE)). //
        hibType(LongType.INSTANCE). //
        category(PropertyCategory.EXPRESSION). //
        expression(expr().prop("key").model()). //
        build();
        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_deduced_id_for_union_entity() throws Exception {
        final ModelledEntityMetadata<TgBogieLocation> entityMetadata = uuu(TgBogieLocation.class);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("id");

        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("id", Long.class, false, pi(TgBogieLocation.class)). //
        hibType(LongType.INSTANCE). //
        category(PropertyCategory.EXPRESSION). //
        expression(expr().caseWhen().prop("wagonSlot").isNotNull().then().prop("wagonSlot.id").when().prop("workshop").isNotNull().then().prop("workshop.id").otherwise().val(null).end().model()). //
        build();
        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_deduced_key_for_union_entity() throws Exception {
        final ModelledEntityMetadata<TgBogieLocation> entityMetadata = uuu(TgBogieLocation.class);
        final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("key");

        final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("key", String.class, false, pi(TgBogieLocation.class)). //
        hibType(StringType.INSTANCE). //
        category(PropertyCategory.EXPRESSION). //
        expression(expr().caseWhen().prop("wagonSlot").isNotNull().then().prop("wagonSlot.key").when().prop("workshop").isNotNull().then().prop("workshop.key").otherwise().val(null).end().model()). //
        build();
        assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_union_entity_generated_models() throws Exception {
        final ModelledEntityMetadata<TgBogieLocation> entityMetadata = uuu(TgBogieLocation.class);

        System.out.println(entityMetadata.getModels());
        assertNull(entityMetadata.getProps().get("datePeriod"));
    }

    @Test
    public void test_props_of_union_entity() throws Exception {
        final PersistedEntityMetadata<TgBogie> bogieEm = ppp(TgBogie.class);

        System.out.println(bogieEm.getProps().get("location"));
        System.out.println(bogieEm.getProps().get("location.workshop"));
        System.out.println(bogieEm.getProps().get("location.wagonSlot"));

        final ModelledEntityMetadata<TgBogieLocation> bogieLocationEm = uuu(TgBogieLocation.class);

        System.out.println(bogieLocationEm.getProps().get("key"));
        System.out.println(bogieLocationEm.getProps().get("workshop"));
        System.out.println(bogieLocationEm.getProps().get("wagonSlot"));
    }
    
    @Test
    public void test() {
        EntityTypeInfo<TgBogie> entityTypeInfo = pi(TgBogie.class);
        final PropertyMetadata bogieLocationProp = new PropertyMetadata.Builder("location", TgBogieLocation.class, true, entityTypeInfo). //
        column(new PropertyColumn("location")). //
        category(UNION_ENTITY_HEADER). //
        build();
        final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
        expected.add(ppi("location.wagonSlot", WAGON_SLOT, true, hibType("long"), "LOCATION_WAGONSLOT", UNION_ENTITY_DETAILS, entityTypeInfo));
        expected.add(ppi("location.workshop", WORKSHOP, true, hibType("long"), "LOCATION_WORKSHOP", UNION_ENTITY_DETAILS, entityTypeInfo));

        final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
        actual.addAll(bogieLocationProp.getComponentTypeSubprops());
        assertEquals("Incorrect result type", expected, actual);
    }
}