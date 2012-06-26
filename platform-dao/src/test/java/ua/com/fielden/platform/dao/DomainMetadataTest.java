package ua.com.fielden.platform.dao;

import org.hibernate.Hibernate;
import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

public class DomainMetadataTest extends BaseEntQueryTCase {

    @Test
    public void test_one_to_one_property_metadata() throws Exception {
	final EntityMetadata vehicleMetadata = DOMAIN_METADATA.generateEntityMetadata(TgVehicle.class);
	final PropertyMetadata actFinDetailsMetadata = vehicleMetadata.getProps().get("finDetails");

	final PropertyMetadata expFinDetailsMetadata = new PropertyMetadata.Builder("finDetails", TgVehicleFinDetails.class, true). //
		hibType(Hibernate.LONG). //
		type(PropertyCategory.IMPLICITLY_CALCULATED). //
		expression(expr().prop("id").model()). //
		build();

	assertEquals("Should be equal", expFinDetailsMetadata, actFinDetailsMetadata);
    }
}
