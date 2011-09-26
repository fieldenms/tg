package ua.com.fielden.platform.test.mapping;

import org.junit.Test;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;


public class MappingGenerationTest {
    @Test
    public void test1() {

	final MappingsGenerator mg = new MappingsGenerator();
	final String tgModelMapping = mg.generateMappings(new Class[]{TgVehicleModel.class});
	System.out.println(tgModelMapping);
    }
}
