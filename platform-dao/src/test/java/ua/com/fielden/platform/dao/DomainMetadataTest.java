package ua.com.fielden.platform.dao;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgMakeCount;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import static org.junit.Assert.assertEquals;

public class DomainMetadataTest extends BaseEntQueryTCase {

    @Test
    public void test_make_metadata() throws Exception {
	final EntityMetadata makeCountMetadata = DOMAIN_METADATA.generateEntityMetadata(TgVehicleMake.class);
	makeCountMetadata.getProps();

	System.out.println(makeCountMetadata.getType());
	System.out.println(makeCountMetadata.getModel());
	System.out.println(makeCountMetadata.getProps());
    }


    @Test
    public void test() throws Exception {
	final EntityMetadata makeCountMetadata = DOMAIN_METADATA.generateEntityMetadata(TgMakeCount.class);
	makeCountMetadata.getProps();

	System.out.println(makeCountMetadata.getType());
	System.out.println(makeCountMetadata.getModel());
	System.out.println(makeCountMetadata.getProps());
    }

    @Test
    @Ignore
    public void test_one_to_one_property_metadata() throws Exception {
	final EntityMetadata vehicleMetadata = DOMAIN_METADATA.generateEntityMetadata(TgVehicle.class);
	final PropertyMetadata actFinDetailsMetadata = vehicleMetadata.getProps().get("finDetails");

	final PropertyMetadata expFinDetailsMetadata = new PropertyMetadata.Builder("finDetails", TgVehicleFinDetails.class, false). //
		type(PropertyCategory.ENTITY). //
		column(new PropertyColumn("_ID")). //
		build();

	assertEquals("Should be equal", expFinDetailsMetadata, actFinDetailsMetadata);
    }
}
