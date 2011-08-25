package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithRangeProperties;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * A test case for validation of range properties.
 *
 * @author TG Team
 *
 */
public class IntRangePropertyValidatorTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);


    @Test
    public void test_int_range_validation_where_only_from_is_set() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromInt(12);
	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should be valid", entity.getProperty("toInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
    }

    @Test
    public void test_int_range_validation_where_only_to_is_set() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setToInt(12);
	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertFalse("Should not be valid", entity.getProperty("toInt").isValid());
    }

    @Test
    public void test_int_range_validation_where_both_from_and_to_are_set_in_the_right_order() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromInt(12);
	entity.setToInt(16);
	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should not be valid", entity.getProperty("toInt").isValid());
	assertEquals("Incorrect value", new Integer(12), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(16), entity.getToInt());
    }

    @Test
    public void test_int_range_validation_where_range_is_set_incorrectly_with_error_recovery() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromInt(16);
	entity.setToInt(12);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertEquals("Incorrect value", new Integer(16), entity.getFromInt());
	assertFalse("Should not be valid", entity.getProperty("toInt").isValid());
	assertNull("Incorrect value", entity.getToInt());

	entity.setFromInt(6);

	assertTrue("Should be valid", entity.getProperty("fromInt").isValid());
	assertTrue("Should not be valid", entity.getProperty("toInt").isValid());

	assertEquals("Incorrect value", new Integer(6), entity.getFromInt());
	assertEquals("Incorrect value", new Integer(12), entity.getToInt());
    }

}
