package ua.com.fielden.platform.dao;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgMakeCount;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;

public class DomainPersistenceMetadataTest extends BaseEntQueryTCase {

    @Test
    public void test_make_metadata() throws Exception {
	final EntityPersistenceMetadata makeCountMetadata = DOMAIN_PERSISTENCE_METADATA.generateEntityPersistenceMetadata(TgVehicleMake.class);
	makeCountMetadata.getProps();

	System.out.println(makeCountMetadata.getType());
	System.out.println(makeCountMetadata.getModel());
	System.out.println(makeCountMetadata.getProps());
    }


    @Test
    public void test() throws Exception {
	final EntityPersistenceMetadata makeCountMetadata = DOMAIN_PERSISTENCE_METADATA.generateEntityPersistenceMetadata(TgMakeCount.class);
	makeCountMetadata.getProps();

	System.out.println(makeCountMetadata.getType());
	System.out.println(makeCountMetadata.getModel());
	System.out.println(makeCountMetadata.getProps());
    }
}
