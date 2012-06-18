package ua.com.fielden.platform.dao;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgMakeCount;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;

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
}
