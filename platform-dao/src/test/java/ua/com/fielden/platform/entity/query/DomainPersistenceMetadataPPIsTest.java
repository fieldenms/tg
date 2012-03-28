package ua.com.fielden.platform.entity.query;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.junit.Test;

import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;
import ua.com.fielden.platform.dao2.PropertyPersistenceInfo.PropertyPersistenceType;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DomainPersistenceMetadataPPIsTest extends BaseEntQueryTCase {
    private static Type hibType(final String name) {
	return TypeFactory.basic(name);
    }

    private static PropertyPersistenceInfo ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final String column, final PropertyPersistenceType type) {
	return new PropertyPersistenceInfo.Builder(name, javaType, nullable).column(column).hibType(hibType).type(type).build();
    }

    private static PropertyPersistenceInfo ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final List<String> columns, final PropertyPersistenceType type) {
	return new PropertyPersistenceInfo.Builder(name, javaType, nullable).columns(columns).hibType(hibType).type(type).build();
    }

    @Test
    public void test1() {
	final SortedSet<PropertyPersistenceInfo> expected = new TreeSet<PropertyPersistenceInfo>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyPersistenceType.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyPersistenceType.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyPersistenceType.PRIMITIVE_KEY));
	expected.add(ppi("desc", STRING, false, hibType("string"), "DESC_", PropertyPersistenceType.PROP));
	expected.add(ppi("make", MAKE, true, hibType("long"), "MAKE_", PropertyPersistenceType.ENTITY));

	final SortedSet<PropertyPersistenceInfo> actual = new TreeSet<PropertyPersistenceInfo>();
	actual.addAll(DOMAIN_PERSISTENCE_METADATA.getEntityPPIs(MODEL));
	assertEquals("Incorrect result type", expected, actual);
    }

    @Test
    public void test2() {
	final SortedSet<PropertyPersistenceInfo> expected = new TreeSet<PropertyPersistenceInfo>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyPersistenceType.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyPersistenceType.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyPersistenceType.PRIMITIVE_KEY));
	expected.add(ppi("desc", STRING, false, hibType("string"), "DESC_", PropertyPersistenceType.PROP));
	expected.add(ppi("model", MODEL, false, hibType("long"), "MODEL_", PropertyPersistenceType.ENTITY));
	expected.add(ppi("price.amount", BIG_DECIMAL, true, hibType("big_decimal"), "PRICE_", PropertyPersistenceType.COMPOSITE_DETAILS));
	expected.add(ppi("purchasePrice.amount", BIG_DECIMAL, true, hibType("big_decimal"), "PURCHASEPRICE_", PropertyPersistenceType.COMPOSITE_DETAILS));

	final SortedSet<PropertyPersistenceInfo> actual = new TreeSet<PropertyPersistenceInfo>();
	actual.addAll(DOMAIN_PERSISTENCE_METADATA.getEntityPPIs(VEHICLE));
	assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test4() {
	final SortedSet<PropertyPersistenceInfo> expected = new TreeSet<PropertyPersistenceInfo>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyPersistenceType.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyPersistenceType.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyPersistenceType.PRIMITIVE_KEY));
	expected.add(ppi("roles", UserRole.class, false, hibType("long"), "ID_CRAFT", PropertyPersistenceType.COLLECTIONAL));

	final SortedSet<PropertyPersistenceInfo> actual = new TreeSet<PropertyPersistenceInfo>();
	actual.addAll(DOMAIN_PERSISTENCE_METADATA.getEntityPPIs(User.class));
	assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test3() {
	assertTrue(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "station"));
	assertTrue(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "station.key"));

	assertFalse(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "model"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "model.id"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "model.key"));
	assertTrue(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "model.desc"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "model.make"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "model.make.id"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "model.make.key"));
	assertTrue(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "model.make.desc"));
	assertTrue(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "price.amount"));
	try {
	    assertTrue(DOMAIN_PERSISTENCE_METADATA.isNullable(VEHICLE, "price.currency"));
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }
}