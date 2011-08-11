package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithRangeProperties;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * A test case for validation of range properties.
 *
 * @author TG Team
 *
 */
public class DoubleRangePropertyValidatorTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private Injector injector = Guice.createInjector(module);
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    {
	factory.setInjector(injector);
    }

    @Test
    public void test_double_range_validation_where_only_from_is_set() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromDouble(12.5);
	assertTrue("Should be valid", entity.getProperty("fromDouble").isValid());
	assertTrue("Should be valid", entity.getProperty("toDouble").isValid());
	assertEquals("Incorrect value", new Double(12.5), entity.getFromDouble());
    }

    @Test
    public void test_double_range_validation_where_only_to_is_set() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setToDouble(12.5);
	assertTrue("Should be valid", entity.getProperty("fromDouble").isValid());
	assertFalse("Should not be valid", entity.getProperty("toDouble").isValid());
    }

    @Test
    public void test_double_range_validation_where_both_from_and_to_are_set_in_the_right_order() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromDouble(12.5);
	entity.setToDouble(16.5);
	assertTrue("Should be valid", entity.getProperty("fromDouble").isValid());
	assertTrue("Should not be valid", entity.getProperty("toDouble").isValid());
	assertEquals("Incorrect value", new Double(12.5), entity.getFromDouble());
	assertEquals("Incorrect value", new Double(16.5), entity.getToDouble());
    }

    @Test
    public void test_double_range_validation_where_range_is_set_incorrectly_with_error_recovery() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromDouble(16.5);
	entity.setToDouble(12.5);

	assertTrue("Should be valid", entity.getProperty("fromDouble").isValid());
	assertEquals("Incorrect value", new Double(16.5), entity.getFromDouble());
	assertFalse("Should not be valid", entity.getProperty("toDouble").isValid());
	assertNull("Incorrect value", entity.getToDouble());

	entity.setFromDouble(6.5);

	assertTrue("Should be valid", entity.getProperty("fromDouble").isValid());
	assertTrue("Should not be valid", entity.getProperty("toDouble").isValid());

	assertEquals("Incorrect value", new Double(6.5), entity.getFromDouble());
	assertEquals("Incorrect value", new Double(12.5), entity.getToDouble());
    }

}
