package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithMultyEndedRangeProperties;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * A test case for validation of multi-range properties.
 *
 * @author TG Team
 *
 */
public class IntMultiRangePropertyValidatorTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_multi_range_validation_where_only_from_is_set() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setFromInt(12);
	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
    }

    @Test
    public void test_multi_range_validation_where_only_to_is_set() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setToInt(12);
	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertFalse("Should not be valid", entity.getProperty("toInt").isValid());
    }

    @Test
    public void test_multi_range_validation_where_only_middle_is_set() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setMiddleInt(12);
	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertFalse("Should not be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
    }

    @Test
    public void test_multi_range_validation_where_both_from_and_to_are_set_in_the_right_order() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setFromInt(12);
	entity.setToInt(16);
	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(16), entity.getToInt());
    }

    @Test
    public void test_multi_range_validation_where_from_and_middle_are_set_in_the_right_order() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setFromInt(12);
	entity.setMiddleInt(16);
	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(16), entity.getMiddleInt());
    }

    @Test
    public void test_multi_range_validation_where_middle_and_to_are_set_in_the_right_order() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");

	entity.setMiddleInt(12);
	entity.setToInt(16);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertFalse("Should not be valid", entity.getProperty("middleInt").isValid());
	assertFalse("Should not be valid", entity.getProperty("toInt").isValid());
	assertNull("Incorrect value", entity.getFromInt());
	assertNull("Incorrect value", entity.getMiddleInt());
	assertNull("Incorrect value", entity.getToInt());
    }

    @Test
    public void test_multi_range_validation_where_range_from_to_is_set_incorrectly_with_error_recovery() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setFromInt(16);
	entity.setToInt(12);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertEquals("Incorrect value", new Integer(16), entity.getFromInt());
	assertFalse("Should not be valid", entity.getProperty("toInt").isValid());
	assertNull("Incorrect value", entity.getToInt());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());

	entity.setFromInt(6);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());

	assertEquals("Incorrect value", new Integer(6), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(12), entity.getToInt());
    }

    @Test
    public void test_multi_range_validation_where_range_from_and_middle_is_set_incorrectly_with_error_recovery() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setFromInt(16);
	entity.setMiddleInt(12);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertEquals("Incorrect value", new Integer(16), entity.getFromInt());
	assertFalse("Should not be valid", entity.getProperty("middleInt").isValid());
	assertNull("Incorrect value", entity.getToInt());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());

	entity.setFromInt(6);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());

	assertEquals("Incorrect value", new Integer(6), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(12), entity.getMiddleInt());
    }

    @Test
    public void test_multi_range_validation_where_range_middle_and_to_is_set_incorrectly_with_error_recovery() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");

	entity.setMiddleInt(12);
	entity.setToInt(16);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertFalse("Should not be valid", entity.getProperty("middleInt").isValid());
	assertFalse("Should not be valid", entity.getProperty("toInt").isValid());
	assertNull("Incorrect value", entity.getFromInt());
	assertNull("Incorrect value", entity.getMiddleInt());
	assertNull("Incorrect value", entity.getToInt());

	entity.setFromInt(6);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());

	assertEquals("Incorrect value", new Integer(6), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(12), entity.getMiddleInt());
	assertEquals("Incorrect value", new Integer(16), entity.getToInt());
    }

    @Test
    public void test_multi_range_validation_where_range_is_set_incorrectly_due_to_middle_value_with_error_recovery() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setFromInt(12);
	entity.setToInt(16);
	entity.setMiddleInt(18);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
	assertEquals("Incorrect value", new Integer(16), entity.getToInt());
	assertFalse("Should not be valid", entity.getProperty("middleInt").isValid());

	entity.setMiddleInt(14);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());

	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(14), entity.getMiddleInt());
	assertEquals("Incorrect value", new Integer(16), entity.getToInt());
    }

    @Test
    public void test_multi_range_validation_where_range_from_to_is_set_correctly_but_middle_incorrectly_with_error_recovery_after_middleInt_is_reset() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setFromInt(12);
	entity.setMiddleInt(10);
	entity.setToInt(16);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
	assertEquals("Incorrect value", new Integer(16), entity.getToInt());
	assertFalse("Should not be valid", entity.getProperty("middleInt").isValid());

	entity.setMiddleInt(14);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());

	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(14), entity.getMiddleInt());
	assertEquals("Incorrect value", new Integer(16), entity.getToInt());
    }

    @Test
    public void test_multi_range_validation_where_range_from_to_is_set_correctly_but_middle_incorrectly_with_error_recovery_after_fromInt_is_reset() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setFromInt(12);
	entity.setMiddleInt(10);
	entity.setToInt(16);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertFalse("Should not be valid", entity.getProperty("middleInt").isValid());
	assertNull("Incorrect value", entity.getMiddleInt());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
	assertEquals("Incorrect value", new Integer(16), entity.getToInt());


	entity.setFromInt(8);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());

	assertEquals("Incorrect value", new Integer(8), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(10), entity.getMiddleInt());
	assertEquals("Incorrect value", new Integer(16), entity.getToInt());
    }

    @Test
    public void test_multi_range_validation_where_range_is_set_incorrectly_due_to_toInt_value() {
	final EntityWithMultyEndedRangeProperties entity = factory.newByKey(EntityWithMultyEndedRangeProperties.class, "key");
	entity.setFromInt(12);
	entity.setMiddleInt(16);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertEquals("Incorrect value", new Integer(16), entity.getMiddleInt());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
	assertNull("Incorrect value", entity.getToInt());

	entity.setToInt(14); // attempting to set incorrect value

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertEquals("Incorrect value", new Integer(16), entity.getMiddleInt());
	assertFalse("Should not be valid", entity.getProperty("toInt").isValid());
	assertNull("Incorrect value", entity.getToInt());

	entity.setToInt(18); // attempting to set correct value

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertTrue("Should be valid", entity.getProperty("middleInt").isValid());
	assertEquals("Incorrect value", new Integer(16), entity.getMiddleInt());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
	assertEquals("Incorrect value", new Integer(18), entity.getToInt());
    }
}
