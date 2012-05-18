package ua.com.fielden.platform.swing.review;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.swing.review.DynamicQueryBuilder.getEmptyValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.UnsupportedTypeException;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * A test for {@link DynamicQueryBuilder}.
 *
 * @author TG Team
 *
 */
public class DynamicQueryBuilderTest {

    private final String alias = "alias";

    @Test
    public void test_empty_value_concept() {
	assertNull("Empty value for single property of AbstractEntity type should be null.", getEmptyValue(AbstractEntity.class, true));
	assertNull("Empty value for single property of AbstractEntity descendant's type should be null.", getEmptyValue(Entity.class, true));
	assertEquals("Empty value for single property of AbstractEntity type should be empty string list.", new ArrayList<String>(), getEmptyValue(AbstractEntity.class, false));
	assertEquals("Empty value for single property of AbstractEntity descendant's type should be empty string list.", new ArrayList<String>(), getEmptyValue(Entity.class, false));

	assertEquals("Empty value for property of String type should be empty string.", "", getEmptyValue(String.class, false));
	assertEquals("Empty value for property of String type should be empty string.", "", getEmptyValue(String.class, true));

	assertEquals("Empty value for property of boolean type should be true.", true, getEmptyValue(boolean.class, false));
	assertEquals("Empty value for property of boolean type should be true.", true, getEmptyValue(boolean.class, true));

	try {
	    getEmptyValue(Boolean.class, false);
	    fail("Boolean.class property should not be supported at this stage.");
	    getEmptyValue(long.class, false);
	    fail("long.class property should not be supported at this stage.");
	} catch (final Exception e) {
	}

	assertEquals("Empty value for property of Number type should be null.", null, getEmptyValue(Number.class, false));
	assertEquals("Empty value for property of Number type should be null.", null, getEmptyValue(Number.class, true));
	assertEquals("Empty value for property of Number descendant's type should be null.", null, getEmptyValue(Long.class, false));
	assertEquals("Empty value for property of Number descendant's type should be null.", null, getEmptyValue(BigDecimal.class, true));
	assertEquals("Empty value for property of Date type should be null.", null, getEmptyValue(Date.class, false));
	assertEquals("Empty value for property of Date type should be null.", null, getEmptyValue(Date.class, true));
	assertEquals("Empty value for property of Money type should be null.", null, getEmptyValue(Money.class, false));
	assertEquals("Empty value for property of Money type should be null.", null, getEmptyValue(Money.class, true));
    }

    @Test
    public void test_QueryProperty_hasEmptyValue_concept() {
	final Class<?> klass = EntityForQueryPropertyTesting.class;
	QueryProperty qp;

	qp = new QueryProperty(klass, "entity1");
	qp.setValue(new ArrayList<String>());
	assertTrue("Non-crit-only AbstractEntity property with empty String list value should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(new ArrayList<String>() { { add("some"); }});
	assertFalse("Non-crit-only AbstractEntity property with non-empty String list value should not 'have empty value'.", qp.hasEmptyValue());


	qp = new QueryProperty(klass, "entity2");
	qp.setValue(new ArrayList<String>());
	assertTrue("Crit-only non-single AbstractEntity property with empty String list value should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(new ArrayList<String>() { { add("some"); }});
	assertFalse("Crit-only non-single AbstractEntity property with non-empty String list value should not 'have empty value'.", qp.hasEmptyValue());

	qp = new QueryProperty(klass, "entity3");
	assertTrue("Crit-only single AbstractEntity property with null value should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some");
	assertFalse("Crit-only single AbstractEntity property with non-null value should not 'have empty value'.", qp.hasEmptyValue());

	qp = new QueryProperty(klass, "firstProperty");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'have empty value'.", qp.hasEmptyValue());

	qp = new QueryProperty(klass, "observableProperty");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'have empty value'.", qp.hasEmptyValue());

	qp = new QueryProperty(klass, "date");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'have empty value'.", qp.hasEmptyValue());

	qp = new QueryProperty(klass, "money");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'have empty value'.", qp.hasEmptyValue());
	qp.setAndBefore(Boolean.TRUE);
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'have empty value'.", qp.hasEmptyValue());
	qp.setAndBefore(Boolean.FALSE);
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'have empty value'.", qp.hasEmptyValue());
	qp.setAndBefore(null);
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'have empty value'.", qp.hasEmptyValue());
	qp.setAndBefore(Boolean.TRUE);
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'have empty value'.", qp.hasEmptyValue());
	qp.setAndBefore(Boolean.FALSE);
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'have empty value'.", qp.hasEmptyValue());
	qp.setAndBefore(null);
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'have empty value'.", qp.hasEmptyValue());

	qp = new QueryProperty(klass, "bool");
	qp.setValue(true);
	qp.setValue2(true);
	assertTrue(qp.getType().getSimpleName() + " property with both true values should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(false);
	qp.setValue2(false);
	assertTrue(qp.getType().getSimpleName() + " property with both false values should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(true);
	qp.setValue2(false);
	assertFalse(qp.getType().getSimpleName() + " property with one true value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(false);
	qp.setValue2(true);
	assertFalse(qp.getType().getSimpleName() + " property with one true value should not 'have empty value'.", qp.hasEmptyValue());

	qp = new QueryProperty(klass, "strProp");
	qp.setValue("");
	assertTrue(qp.getType().getSimpleName() + " property with empty String value should 'have empty value'.", qp.hasEmptyValue());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with non-empty String value should not 'have empty value'.", qp.hasEmptyValue());
	qp.setValue(null);
	assertFalse(qp.getType().getSimpleName() + " property with null value should not 'have empty value'.", qp.hasEmptyValue());
    }

    @Test
    public void test_QueryProperty_isEmpty_concept() {
	final Class<?> klass = EntityForQueryPropertyTesting.class;
	QueryProperty qp;

	qp = new QueryProperty(klass, "entity1");
	qp.setValue(new ArrayList<String>());
	assertTrue("Non-crit-only AbstractEntity property with empty String list value should 'be empty'.", qp.isEmpty());
	qp.setValue(new ArrayList<String>() { { add("some"); }});
	assertFalse("Non-crit-only AbstractEntity property with non-empty String list value should not 'be empty'.", qp.isEmpty());


	qp = new QueryProperty(klass, "entity2");
	qp.setValue(new ArrayList<String>());
	assertTrue("Crit-only non-single AbstractEntity property with empty String list value should 'be empty'.", qp.isEmpty());
	qp.setValue(new ArrayList<String>() { { add("some"); }});
	assertFalse("Crit-only non-single AbstractEntity property with non-empty String list value should not 'be empty'.", qp.isEmpty());

	qp = new QueryProperty(klass, "entity3");
	assertTrue("Crit-only single AbstractEntity property with null value should 'be empty'.", qp.isEmpty());
	qp.setValue("some");
	assertFalse("Crit-only single AbstractEntity property with non-null value should not 'be empty'.", qp.isEmpty());

	qp = new QueryProperty(klass, "firstProperty");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be empty'.", qp.isEmpty());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be empty'.", qp.isEmpty());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be empty'.", qp.isEmpty());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be empty'.", qp.isEmpty());

	qp = new QueryProperty(klass, "observableProperty");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be empty'.", qp.isEmpty());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be empty'.", qp.isEmpty());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be empty'.", qp.isEmpty());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be empty'.", qp.isEmpty());

	qp = new QueryProperty(klass, "date");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be empty'.", qp.isEmpty());

	qp.setAndBefore(Boolean.TRUE);
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be empty'.", qp.isEmpty());
	qp.setAndBefore(Boolean.FALSE);
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be empty'.", qp.isEmpty());
	qp.setAndBefore(null);
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be empty'.", qp.isEmpty());

	qp.setDatePrefix(DateRangePrefixEnum.CURR);
	qp.setDateMnemonic(MnemonicEnum.DAY);
	assertFalse(qp.getType().getSimpleName() + " property with both null values and non-null mnemonics should not 'be empty'.", qp.isEmpty());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be empty'.", qp.isEmpty());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be empty'.", qp.isEmpty());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be empty'.", qp.isEmpty());

	qp.setAndBefore(Boolean.TRUE);
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be empty'.", qp.isEmpty());
	qp.setAndBefore(Boolean.FALSE);
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be empty'.", qp.isEmpty());
	qp.setAndBefore(null);
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be empty'.", qp.isEmpty());

	qp = new QueryProperty(klass, "money");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be empty'.", qp.isEmpty());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be empty'.", qp.isEmpty());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be empty'.", qp.isEmpty());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be empty'.", qp.isEmpty());

	qp = new QueryProperty(klass, "bool");
	qp.setValue(true);
	qp.setValue2(true);
	assertTrue(qp.getType().getSimpleName() + " property with both true values should 'be empty'.", qp.isEmpty());
	qp.setValue(false);
	qp.setValue2(false);
	assertTrue(qp.getType().getSimpleName() + " property with both false values should 'be empty'.", qp.isEmpty());
	qp.setValue(true);
	qp.setValue2(false);
	assertFalse(qp.getType().getSimpleName() + " property with one true value should not 'be empty'.", qp.isEmpty());
	qp.setValue(false);
	qp.setValue2(true);
	assertFalse(qp.getType().getSimpleName() + " property with one true value should not 'be empty'.", qp.isEmpty());

	qp = new QueryProperty(klass, "strProp");
	qp.setValue("");
	assertTrue(qp.getType().getSimpleName() + " property with empty String value should 'be empty'.", qp.isEmpty());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with non-empty String value should not 'be empty'.", qp.isEmpty());
	qp.setValue(null);
	assertFalse(qp.getType().getSimpleName() + " property with null value should not 'be empty'.", qp.isEmpty());
    }

    @Test
    public void test_QueryProperty_shouldBeIgnored_concept() {
	final Class<?> klass = EntityForQueryPropertyTesting.class;
	QueryProperty qp;

	qp = new QueryProperty(klass, "entity1");
	qp.setValue(new ArrayList<String>());
	assertTrue("Non-crit-only AbstractEntity property with empty String list value should 'be ignored'.", qp.shouldBeIgnored());

	qp.setOrNull(true);
	assertFalse("Property with orNull == true should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(false);
	assertTrue("Property with orNull == false should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(null);

	qp.setValue(new ArrayList<String>() { { add("some"); }});
	assertFalse("Non-crit-only AbstractEntity property with non-empty String list value should not 'be ignored'.", qp.shouldBeIgnored());

	qp = new QueryProperty(klass, "entity2");
	qp.setValue(new ArrayList<String>());
	assertTrue("Crit-only property should 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue(new ArrayList<String>() { { add("some"); }});
	assertTrue("Crit-only property should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(true);
	assertTrue("Crit-only property should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(false);
	assertTrue("Crit-only property should 'be ignored'.", qp.shouldBeIgnored());

	qp = new QueryProperty(klass, "entity3");
	assertTrue("Crit-only property should 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue("some");
	assertTrue("Crit-only property should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(true);
	assertTrue("Crit-only property should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(false);
	assertTrue("Crit-only property should 'be ignored'.", qp.shouldBeIgnored());

	qp = new QueryProperty(klass, "firstProperty");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be ignored'.", qp.shouldBeIgnored());

	qp.setOrNull(true);
	assertFalse("Property with orNull == true should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(false);
	assertTrue("Property with orNull == false should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(null);

	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be ignored'.", qp.shouldBeIgnored());

	qp = new QueryProperty(klass, "observableProperty");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be ignored'.", qp.shouldBeIgnored());

	qp.setOrNull(true);
	assertFalse("Property with orNull == true should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(false);
	assertTrue("Property with orNull == false should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(null);

	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be ignored'.", qp.shouldBeIgnored());

	qp = new QueryProperty(klass, "date");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be ignored'.", qp.shouldBeIgnored());

	qp.setOrNull(true);
	assertFalse("Property with orNull == true should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(false);
	assertTrue("Property with orNull == false should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(null);

	qp.setDatePrefix(DateRangePrefixEnum.CURR);
	qp.setDateMnemonic(MnemonicEnum.DAY);
	assertFalse(qp.getType().getSimpleName() + " property with both null values and non-null mnemonics should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be ignored'.", qp.shouldBeIgnored());

	qp = new QueryProperty(klass, "money");
	assertTrue(qp.getType().getSimpleName() + " property with both null values should 'be ignored'.", qp.shouldBeIgnored());

	qp.setOrNull(true);
	assertFalse("Property with orNull == true should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(false);
	assertTrue("Property with orNull == false should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(null);

	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue(null);
	qp.setValue2("some");
	assertFalse(qp.getType().getSimpleName() + " property with one non-null value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue("some1");
	qp.setValue2("some2");
	assertFalse(qp.getType().getSimpleName() + " property with both non-null values should not 'be ignored'.", qp.shouldBeIgnored());

	qp = new QueryProperty(klass, "bool");
	qp.setValue(true);
	qp.setValue2(true);
	assertTrue(qp.getType().getSimpleName() + " property with both true values should 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue(false);
	qp.setValue2(false);
	assertTrue(qp.getType().getSimpleName() + " property with both false values should 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue(true);
	qp.setValue2(false);
	assertFalse(qp.getType().getSimpleName() + " property with one true value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue(false);
	qp.setValue2(true);
	assertFalse(qp.getType().getSimpleName() + " property with one true value should not 'be ignored'.", qp.shouldBeIgnored());

	qp = new QueryProperty(klass, "strProp");
	qp.setValue("");
	assertTrue(qp.getType().getSimpleName() + " property with empty String value should 'be ignored'.", qp.shouldBeIgnored());

	qp.setOrNull(true);
	assertFalse("Property with orNull == true should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(false);
	assertTrue("Property with orNull == false should 'be ignored'.", qp.shouldBeIgnored());
	qp.setOrNull(null);

	qp.setValue("some");
	assertFalse(qp.getType().getSimpleName() + " property with non-empty String value should not 'be ignored'.", qp.shouldBeIgnored());
	qp.setValue(null);
	assertFalse(qp.getType().getSimpleName() + " property with null value should not 'be ignored'.", qp.shouldBeIgnored());
    }

    @Test
    public void test_QueryProperty_meta_information_determination() {
	final Class<?> klass = EntityForQueryPropertyTesting.class;
	QueryProperty qp = null;

	try {
	    qp = new QueryProperty(klass, "unsupportedProp1");
	    fail("The type of property should not be supported.");
	} catch (final UnsupportedTypeException e) {
	}

	try {
	    qp = new QueryProperty(klass, "unsupportedProp2");
	    fail("The type of property should not be supported.");
	} catch (final UnsupportedTypeException e) {
	}

	qp = new QueryProperty(klass, "entity1");
	assertEquals("Incorrect type.", EntityForQueryPropertyTesting.class, qp.getType());
	assertFalse("Incorrect 'crit-only' parameter.", qp.isCritOnly());
	assertFalse("Incorrect 'single' parameter.", qp.isSingle());

	qp = new QueryProperty(klass, "entity2");
	assertEquals("Incorrect type.", EntityForQueryPropertyTesting.class, qp.getType());
	assertTrue("Incorrect 'crit-only' parameter.", qp.isCritOnly());
	assertFalse("Incorrect 'single' parameter.", qp.isSingle());

	qp = new QueryProperty(klass, "entity3");
	assertEquals("Incorrect type.", EntityForQueryPropertyTesting.class, qp.getType());
	assertTrue("Incorrect 'crit-only' parameter.", qp.isCritOnly());
	assertTrue("Incorrect 'single' parameter.", qp.isSingle());

	qp = new QueryProperty(klass, "entity3.entities.entity2.entity1");
	assertEquals("Incorrect type.", EntityForQueryPropertyTesting.class, qp.getType());
	assertFalse("Incorrect 'crit-only' parameter.", qp.isCritOnly());
	assertFalse("Incorrect 'single' parameter.", qp.isSingle());

	qp = new QueryProperty(klass, "entity3.entities.entity1.entity2");
	assertEquals("Incorrect type.", EntityForQueryPropertyTesting.class, qp.getType());
	assertTrue("Incorrect 'crit-only' parameter.", qp.isCritOnly());
	assertFalse("Incorrect 'single' parameter.", qp.isSingle());

	qp = new QueryProperty(klass, "entity1.entities.entity2.entity3");
	assertEquals("Incorrect type.", EntityForQueryPropertyTesting.class, qp.getType());
	assertTrue("Incorrect 'crit-only' parameter.", qp.isCritOnly());
	assertTrue("Incorrect 'single' parameter.", qp.isSingle());
    }

    @Test
    public void test_QueryProperty_collectional_meta_information_determination() {
	final Class<?> klass = EntityForQueryPropertyTesting.class;
	QueryProperty qp = null;

	String propertyName = null;
	qp = new QueryProperty(klass, propertyName = "entity1.entity3");
	assertFalse("Should not be within collectional hierarchy.", qp.isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL());
	assertEquals("Incorrect condition building name.", DynamicQueryBuilder.ALIAS + "." + propertyName + ".key", qp.getConditionBuildingName());
	qp = new QueryProperty(klass, propertyName = "entity2.firstProperty");
	assertFalse("Should not be within collectional hierarchy.", qp.isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL());
	assertEquals("Incorrect condition building name.", DynamicQueryBuilder.ALIAS + "." + propertyName, qp.getConditionBuildingName());
	qp = new QueryProperty(klass, propertyName = "strProp");
	assertFalse("Should not be within collectional hierarchy.", qp.isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL());
	assertEquals("Incorrect condition building name.", DynamicQueryBuilder.ALIAS + "." + propertyName, qp.getConditionBuildingName());

	try {
	    qp = new QueryProperty(klass, "entity1.entity3.coll");
	    fail("The collection itself could not be used for quering.");
	} catch (final Exception e) {
	}
	try {
	    qp = new QueryProperty(klass, "entity1.coll.entity3.entities.entity2");
	    fail("Properties in nested collections are not supported at this stage.");
	} catch (final Exception e) {
	}
	try {
	    qp = new QueryProperty(klass, "entity1.coll.entity3.entities");
	    fail("Properties in nested collections are not supported at this stage.");
	} catch (final Exception e) {
	}
	try {
	    qp = new QueryProperty(klass, "entity1.coll.entity3.entities.coll");
	    fail("Properties in nested collections are not supported at this stage.");
	} catch (final Exception e) {
	}

	qp = new QueryProperty(klass, "entity1.entity3.coll.strProp");
	assertTrue("Should be within collectional hierarchy.", qp.isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL());
	assertEquals("Incorrect condition building name.", "strProp", qp.getConditionBuildingName());
	assertEquals("Incorrect collection container type.", CollectionParentEntity.class, qp.getCollectionContainerType());
	assertEquals("Incorrect collection container parent type.", EntityForQueryPropertyTesting.class, qp.getCollectionContainerParentType());
	assertEquals("Incorrect name of collection parent.", "entity1.entity3", qp.getPropertyNameOfCollectionParent());

	qp = new QueryProperty(klass, "entity2.entity1.entities.entity2.strProp");
	assertTrue("Should be within collectional hierarchy.", qp.isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL());
	assertEquals("Incorrect condition building name.", "entity2.strProp", qp.getConditionBuildingName());
	assertEquals("Incorrect collection container type.", EntityForQueryPropertyTesting.class, qp.getCollectionContainerType());
	assertEquals("Incorrect collection container parent type.", EntityForQueryPropertyTesting.class, qp.getCollectionContainerParentType());
	assertEquals("Incorrect name of collection parent.", "entity2.entity1", qp.getPropertyNameOfCollectionParent());
    }
}
