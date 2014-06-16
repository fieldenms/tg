package ua.com.fielden.platform.test.mapping;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.HibernateMappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;

public class MappingGenerationTest {
    @Test
    public void dump_mapping_for_type_wity_byte_array_property() {
        final List<Class<? extends AbstractEntity<?>>> domainTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();
        domainTypes.add(EntityCentreConfig.class);
        final DomainMetadata mg = new DomainMetadata(null, null, domainTypes, DbVersion.H2);
        final String tgModelMapping = new HibernateMappingsGenerator().generateMappings(mg);
        final String expectedMapping = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!DOCTYPE hibernate-mapping PUBLIC\n" + "\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n"
                + "\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" + "<hibernate-mapping default-access=\"field\">\n"
                + "<class name=\"ua.com.fielden.platform.ui.config.EntityCentreConfig\" table=\"ENTITY_CENTRE_CONFIG\">\n"
                + "	<id name=\"id\" column=\"_ID\" type=\"org.hibernate.type.LongType\" access=\"property\">\n" + "		<generator class=\"hilo\">\n"
                + "			<param name=\"table\">UNIQUE_ID</param>\n" + "			<param name=\"column\">NEXT_VALUE</param>\n" + "			<param name=\"max_lo\">0</param>\n" + "		</generator>\n"
                + "	</id>\n" + "	<version name=\"version\" type=\"org.hibernate.type.LongType\" access=\"field\" insert=\"false\">\n"
                + "		<column name=\"_VERSION\" default=\"0\" />\n" + "	</version>\n"
                + "	<property name=\"configBody\" column=\"BODY\" type=\"org.hibernate.type.BinaryType\" length=\"1073741824\"/>\n"
                + "	<many-to-one name=\"menuItem\" class=\"ua.com.fielden.platform.ui.config.MainMenuItem\" column=\"ID_MAIN_MENU\"/>\n"
                + "	<many-to-one name=\"owner\" class=\"ua.com.fielden.platform.security.user.User\" column=\"ID_CRAFT\"/>\n"
                + "	<property name=\"principal\" column=\"IS_PRINCIPAL\" type=\"org.hibernate.type.BooleanType\"/>\n"
                + "	<property name=\"title\" column=\"TITLE\" type=\"org.hibernate.type.StringType\"/>\n" + "</class>\n\n" + "</hibernate-mapping>";
        assertEquals("Incorrect mapping.", expectedMapping, tgModelMapping);
    }
}