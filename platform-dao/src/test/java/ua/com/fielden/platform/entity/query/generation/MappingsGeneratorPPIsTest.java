package ua.com.fielden.platform.entity.query.generation;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.dao.PropertyPersistenceType;
import static org.junit.Assert.assertEquals;

public class MappingsGeneratorPPIsTest extends BaseEntQueryTCase {
    private static Type hibType(final String name) {
	return TypeFactory.basic(name);
    }

    private static PropertyPersistenceInfo ppi(final String name, final Class javaType, final Object hibType, final String column, final PropertyPersistenceType type) {
	return new PropertyPersistenceInfo.Builder(name, javaType).column(column).hibType(hibType).type(type).build();
    }

    private static PropertyPersistenceInfo ppi(final String name, final Class javaType, final Object hibType, final List<String> columns, final PropertyPersistenceType type) {
	return new PropertyPersistenceInfo.Builder(name, javaType).columns(columns).hibType(hibType).type(type).build();
    }

    @Test
    public void test1() {
	final SortedSet<PropertyPersistenceInfo> expected = new TreeSet<PropertyPersistenceInfo>();
	expected.add(ppi("id", LONG, hibType("long"), "_ID", PropertyPersistenceType.ID));
	expected.add(ppi("version", LONG, hibType("long"), "_VERSION", PropertyPersistenceType.VERSION));
	expected.add(ppi("key", STRING, hibType("string"), "KEY_", PropertyPersistenceType.PRIMITIVE_KEY));
	expected.add(ppi("desc", STRING, hibType("string"), "DESC_", PropertyPersistenceType.PROP));
	expected.add(ppi("make", MAKE, hibType("long"), "MAKE_", PropertyPersistenceType.ENTITY));

	final SortedSet<PropertyPersistenceInfo> actual = new TreeSet<PropertyPersistenceInfo>();
	actual.addAll(MAPPINGS_GENERATOR.getEntityPPIs(MODEL));
	assertEquals("Incorrect result type", expected, actual);
    }
}
