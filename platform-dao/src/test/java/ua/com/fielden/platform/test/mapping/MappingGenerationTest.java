package ua.com.fielden.platform.test.mapping;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import static org.junit.Assert.assertEquals;


public class MappingGenerationTest {
    @Test
    public void test1() {

	final List<Class<? extends AbstractEntity>> domainTypes = new ArrayList<Class<? extends AbstractEntity>>();
	domainTypes.add(TgVehicleModel.class);
	final MappingsGenerator mg = new MappingsGenerator(domainTypes);
	final String tgModelMapping = mg.generateMappings();
	System.out.println(tgModelMapping);
    }

    @Test
    public void dump_mapping_for_type_wity_byte_array_property() {
	final List<Class<? extends AbstractEntity>> domainTypes = new ArrayList<Class<? extends AbstractEntity>>();
	domainTypes.add(EntityCentreConfig.class);
	final MappingsGenerator mg = new MappingsGenerator(domainTypes);
	final String tgModelMapping = mg.generateMappings();
	final String expectedMapping =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<!DOCTYPE hibernate-mapping PUBLIC\n" +
                        "\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
                        "\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
                        "<hibernate-mapping default-access=\"field\">\n" +
                        "<class name=\"ua.com.fielden.platform.ui.config.EntityCentreConfig\" table=\"ENTITY_CENTRE_CONFIG\">\n" +
                        "	<id name=\"id\" column=\"_ID\" type=\"long\" access=\"property\">\n" +
                        "		<generator class=\"hilo\">\n" +
                        "			<param name=\"table\">UNIQUE_ID</param>\n" +
                        "			<param name=\"column\">NEXT_VALUE</param>\n" +
                        "			<param name=\"max_lo\">0</param>\n" +
                        "		</generator>\n" +
                        "	</id>\n" +
                        "	<version name=\"version\" type=\"long\" access=\"field\" insert=\"false\">\n" +
                        "		<column name=\"_VERSION\" default=\"0\" />\n" +
                        "	</version>\n" +
                        "	<many-to-one name=\"owner\" class=\"ua.com.fielden.platform.security.user.User\" column=\"ID_CRAFT\"/>\n" +
                        "	<property name=\"title\" column=\"TITLE\"/>\n" +
                        "	<many-to-one name=\"menuItem\" class=\"ua.com.fielden.platform.ui.config.MainMenuItem\" column=\"ID_MAIN_MENU\"/>\n" +
                        "	<property name=\"principal\" column=\"IS_PRINCIPAL\"/>\n" +
                        "	<property name=\"configBody\" column=\"BODY\" length=\"1073741824\"/>\n" +
                        "</class>\n\n"+
                        "</hibernate-mapping>";
	assertEquals("Incorrect mapping.", expectedMapping, tgModelMapping);
    }
}
