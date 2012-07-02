package ua.com.fielden.platform.dao;

import org.hibernate.Hibernate;
import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

public class DomainMetadataTest extends BaseEntQueryTCase {

    @Test
    public void test_one_to_one_property_metadata() throws Exception {
	final EntityMetadata entityMetadata = DOMAIN_METADATA.generateEntityMetadata(TgVehicle.class);
	final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("finDetails");

	final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("finDetails", TgVehicleFinDetails.class, true). //
		hibType(Hibernate.LONG). //
		type(PropertyCategory.IMPLICITLY_CALCULATED). //
		expression(expr().prop("id").model()). //
		build();

	assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_that_critonly_props_are_excluded() throws Exception {
	final EntityMetadata entityMetadata = DOMAIN_METADATA.generateEntityMetadata(TgAverageFuelUsage.class);
	assertNull(entityMetadata.getProps().get("datePeriod"));
    }

    @Test
    public void test_one_to_one_property_metadata_for_synthetic_entity() throws Exception {
	final EntityMetadata entityMetadata = DOMAIN_METADATA.generateEntityMetadata(TgAverageFuelUsage.class);
	final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("key");
	final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("key", TgVehicle.class, false). //
		hibType(Hibernate.LONG). //
		type(PropertyCategory.SYNTHETIC). //
		//expression(expr().prop("key").model()). //
		build();
	assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }

    @Test
    public void test_deduced_id_for_synthetic_entity() throws Exception {
	final EntityMetadata entityMetadata = DOMAIN_METADATA.generateEntityMetadata(TgAverageFuelUsage.class);
	final PropertyMetadata actPropertyMetadata = entityMetadata.getProps().get("id");
	final PropertyMetadata expPropertyMetadata = new PropertyMetadata.Builder("id", Long.class, false). //
		hibType(Hibernate.LONG). //
		type(PropertyCategory.CALCULATED). //
		expression(expr().prop("key").model()). //
		build();
	assertEquals("Should be equal", expPropertyMetadata, actPropertyMetadata);
    }


}