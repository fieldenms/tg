package ua.com.fielden.platform.eql.meta;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyColumn;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgMakeCount;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DomainMetadataPPIsTest extends BaseEntQueryTCase {
    @Test
    public void test1() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyCategory.PRIMITIVE_AS_KEY));
	expected.add(ppi("desc", STRING, true, hibType("string"), "DESC_", PropertyCategory.PRIMITIVE));
	expected.add(ppi("make", MAKE, false, hibType("long"), "MAKE_", PropertyCategory.ENTITY));
//	expected.add(ppi("referencesCount", INTEGER, false, hibType("integer"), Collections.<PropertyColumn> emptyList(), PropertyCategory.EXPRESSION_COMMON));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(MODEL));
	assertEquals("Incorrect result type", expected, actual);
    }

    @Test
    public void test2() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyCategory.PRIMITIVE_AS_KEY));
	expected.add(ppi("desc", STRING, true, hibType("string"), "DESC_", PropertyCategory.PRIMITIVE));
	expected.add(ppi("model", MODEL, false, hibType("long"), "MODEL_", PropertyCategory.ENTITY));
	expected.add(ppi("price.amount", BIG_DECIMAL, true, hibType("big_decimal"), "PRICE_", PropertyCategory.COMPONENT_DETAILS));
	expected.add(ppi("purchasePrice.amount", BIG_DECIMAL, true, hibType("big_decimal"), "PURCHASEPRICE_", PropertyCategory.COMPONENT_DETAILS));
	expected.add(ppi("fuelUsages", FUEL_USAGE, true, null, Collections.<PropertyColumn> emptyList(), PropertyCategory.COLLECTIONAL));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(VEHICLE));
	assertTrue(actual.containsAll(expected));
	System.out.println(actual);
    }

    @Test
    public void test5() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	//expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyPersistenceType.PRIMITIVE_KEY));
	//expected.add(ppi("desc", STRING, false, hibType("string"), "DESC_", PropertyPersistenceType.PROP));
	expected.add(ppi("vehicle", VEHICLE, false, hibType("long"), "VEHICLE_", PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY));
	expected.add(ppi("date", DATE, false, DOMAIN_METADATA_ANALYSER.getDomainMetadata().getHibTypesDefaults().get(Date.class), "DATE_", PropertyCategory.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(FUEL_USAGE));

	assertTrue(actual.containsAll(expected));
    }


    @Test
    public void test4() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "USER_NAME", PropertyCategory.PRIMITIVE_AS_KEY));
	expected.add(ppi("roles", UserAndRoleAssociation.class, true, null, Collections.<PropertyColumn> emptyList(), PropertyCategory.COLLECTIONAL));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(User.class));

	assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test3() {
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "station"));
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "station.key"));

	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.id"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.key"));
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.desc"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.id"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.key"));
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.desc"));
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "price.amount"));
	try {
	    assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "price.currency"));
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test6() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "USER_NAME", PropertyCategory.PRIMITIVE_AS_KEY));
	expected.add(ppi("roles", UserAndRoleAssociation.class, false, null, Collections.<PropertyColumn> emptyList(), PropertyCategory.COLLECTIONAL));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(TgMakeCount.class));

	//System.out.println(actual);
	final PropertyMetadata ppi = DOMAIN_METADATA_ANALYSER.getPropPersistenceInfoExplicitly(TgMakeCount.class, "key");
	System.out.println(ppi);
	//assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test7() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "USER_NAME", PropertyCategory.PRIMITIVE_AS_KEY));
	expected.add(ppi("roles", UserAndRoleAssociation.class, false, null, Collections.<PropertyColumn> emptyList(), PropertyCategory.COLLECTIONAL));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(TgBogie.class));

	System.out.println(actual);
//	final PropertyMetadata ppi = DOMAIN_METADATA_ANALYSER.getPropPersistenceInfoExplicitly(TgMakeCount.class, "key");
//	System.out.println(ppi);
	//assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test_getting_of_leaf_props_from_first_level_props() {
	final Set<String> firstLevelProps = new HashSet<String>();
	firstLevelProps.add("leased");
	firstLevelProps.add("lastMeterReading");
	firstLevelProps.add("lastFuelUsage");

	final Set<String> actual = DOMAIN_METADATA_ANALYSER.getLeafPropsFromFirstLevelProps(null, VEHICLE, firstLevelProps);
	final Set<String> expected = new HashSet<String>();
	expected.add("leased");
	expected.add("lastMeterReading");
	expected.add("lastFuelUsage.vehicle");
	expected.add("lastFuelUsage.date");
	assertEquals(expected, actual);
    }
}