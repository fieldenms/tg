package ua.com.fielden.platform.test.mapping;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;


public class MappingGenerationTest {
    @Test
    public void test1() {

	final MappingsGenerator mg = new MappingsGenerator();
	final List<Class<? extends AbstractEntity>> domainTypes = new ArrayList<Class<? extends AbstractEntity>>();
	domainTypes.add(TgVehicleModel.class);
	final String tgModelMapping = mg.generateMappings(domainTypes);
	System.out.println(tgModelMapping);
    }
}
