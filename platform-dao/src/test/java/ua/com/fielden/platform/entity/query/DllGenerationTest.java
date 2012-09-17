package ua.com.fielden.platform.entity.query;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.EntityMetadata;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import static org.junit.Assert.assertEquals;

public class DllGenerationTest extends BaseEntQueryTCase {
    @Test
    @Ignore
    public void test1() {
	final EntityMetadata<TgVehicleMake> metadata = DOMAIN_METADATA_ANALYSER.getEntityMetadata(MAKE);
	assertEquals("CREATE TABLE TGVEHICLEMAKE_", metadata.ddl());
    }
}