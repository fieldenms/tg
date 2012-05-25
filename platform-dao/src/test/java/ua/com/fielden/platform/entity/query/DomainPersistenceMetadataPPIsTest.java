package ua.com.fielden.platform.entity.query;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo.PropertyPersistenceType;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
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
	expected.add(ppi("desc", STRING, true, hibType("string"), "DESC_", PropertyPersistenceType.PROP));
	expected.add(ppi("make", MAKE, false, hibType("long"), "MAKE_", PropertyPersistenceType.ENTITY));

	final SortedSet<PropertyPersistenceInfo> actual = new TreeSet<PropertyPersistenceInfo>();
	actual.addAll(DOMAIN_PERSISTENCE_METADATA_ANALYSER.getEntityPPIs(MODEL));
	assertEquals("Incorrect result type", expected, actual);
    }

    @Test
    public void test2() {
	final SortedSet<PropertyPersistenceInfo> expected = new TreeSet<PropertyPersistenceInfo>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyPersistenceType.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyPersistenceType.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyPersistenceType.PRIMITIVE_KEY));
	expected.add(ppi("desc", STRING, true, hibType("string"), "DESC_", PropertyPersistenceType.PROP));
	expected.add(ppi("model", MODEL, false, hibType("long"), "MODEL_", PropertyPersistenceType.ENTITY));
	expected.add(ppi("price.amount", BIG_DECIMAL, true, hibType("big_decimal"), "PRICE_", PropertyPersistenceType.COMPOSITE_DETAILS));
	expected.add(ppi("purchasePrice.amount", BIG_DECIMAL, true, hibType("big_decimal"), "PURCHASEPRICE_", PropertyPersistenceType.COMPOSITE_DETAILS));
	expected.add(ppi("fuelUsages", FUEL_USAGE, false, null, Collections.<String> emptyList(), PropertyPersistenceType.COLLECTIONAL));

	final SortedSet<PropertyPersistenceInfo> actual = new TreeSet<PropertyPersistenceInfo>();
	actual.addAll(DOMAIN_PERSISTENCE_METADATA_ANALYSER.getEntityPPIs(VEHICLE));
	assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test5() {
	final SortedSet<PropertyPersistenceInfo> expected = new TreeSet<PropertyPersistenceInfo>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyPersistenceType.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyPersistenceType.VERSION));
	//expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyPersistenceType.PRIMITIVE_KEY));
	//expected.add(ppi("desc", STRING, false, hibType("string"), "DESC_", PropertyPersistenceType.PROP));
	expected.add(ppi("vehicle", VEHICLE, false, hibType("long"), "VEHICLE_", PropertyPersistenceType.ENTITY_MEMBER_OF_COMPOSITE_KEY));
	expected.add(ppi("date", DATE, false, DOMAIN_PERSISTENCE_METADATA_ANALYSER.getDomainPersistenceMetadata().getHibTypesDefaults().get(Date.class), "DATE_", PropertyPersistenceType.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY));

	final SortedSet<PropertyPersistenceInfo> actual = new TreeSet<PropertyPersistenceInfo>();
	actual.addAll(DOMAIN_PERSISTENCE_METADATA_ANALYSER.getEntityPPIs(FUEL_USAGE));

	assertTrue(actual.containsAll(expected));
    }


    @Test
    public void test4() {
	final SortedSet<PropertyPersistenceInfo> expected = new TreeSet<PropertyPersistenceInfo>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyPersistenceType.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyPersistenceType.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "USER_NAME", PropertyPersistenceType.PRIMITIVE_KEY));
	expected.add(ppi("roles", UserAndRoleAssociation.class, false, null, Collections.<String> emptyList(), PropertyPersistenceType.COLLECTIONAL));

	final SortedSet<PropertyPersistenceInfo> actual = new TreeSet<PropertyPersistenceInfo>();
	actual.addAll(DOMAIN_PERSISTENCE_METADATA_ANALYSER.getEntityPPIs(User.class));

	assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test3() {
	assertTrue(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "station"));
	assertTrue(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "station.key"));

	assertFalse(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "model"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "model.id"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "model.key"));
	assertTrue(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "model.desc"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "model.make"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.id"));
	assertFalse(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.key"));
	assertTrue(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.desc"));
	assertTrue(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "price.amount"));
	try {
	    assertTrue(DOMAIN_PERSISTENCE_METADATA_ANALYSER.isNullable(VEHICLE, "price.currency"));
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }
}