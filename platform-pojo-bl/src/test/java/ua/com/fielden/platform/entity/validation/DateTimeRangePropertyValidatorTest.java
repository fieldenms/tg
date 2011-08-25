package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
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
public class DateTimeRangePropertyValidatorTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_date_range_validation_where_only_from_is_set() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromDateTime(new DateTime(2000, 01, 01, 0, 0, 0, 0));
	assertTrue("Should be valid", entity.getProperty("fromDateTime").isValid());
	assertTrue("Should be valid", entity.getProperty("toDateTime").isValid());
	assertEquals("Incorrect value", new DateTime(2000, 01, 01, 0, 0, 0, 0), entity.getFromDateTime());
    }

    @Test
    public void test_date_range_validation_where_only_to_is_set() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setToDateTime(new DateTime(2000, 01, 01, 0, 0, 0, 0));
	assertTrue("Should be valid", entity.getProperty("fromDateTime").isValid());
	assertFalse("Should not be valid", entity.getProperty("toDateTime").isValid());
    }

    @Test
    public void test_date_range_validation_where_both_from_and_to_are_set_in_the_right_order() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromDateTime(new DateTime(2000, 01, 01, 0, 0, 0, 0));
	entity.setToDateTime(new DateTime(2000, 02, 01, 0, 0, 0, 0));
	assertTrue("Should be valid", entity.getProperty("fromDateTime").isValid());
	assertTrue("Should not be valid", entity.getProperty("toDateTime").isValid());
	assertEquals("Incorrect value", new DateTime(2000, 01, 01, 0, 0, 0, 0), entity.getFromDateTime());
	assertEquals("Incorrect value", new DateTime(2000, 02, 01, 0, 0, 0, 0), entity.getToDateTime());
    }

    @Test
    public void test_date_range_validation_where_range_is_set_incorrectly_with_error_recovery() {
	final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
	entity.setFromDateTime(new DateTime(2000, 02, 01, 0, 0, 0, 0));
	entity.setToDateTime(new DateTime(2000, 01, 01, 0, 0, 0, 0));

	assertTrue("Should be valid", entity.getProperty("fromDateTime").isValid());
	assertEquals("Incorrect value", new DateTime(2000, 02, 01, 0, 0, 0, 0), entity.getFromDateTime());
	assertFalse("Should not be valid", entity.getProperty("toDateTime").isValid());
	assertNull("Incorrect value", entity.getToDateTime());

	entity.setFromDateTime(new DateTime(1999, 01, 01, 0, 0, 0, 0));

	assertTrue("Should be valid", entity.getProperty("fromDateTime").isValid());
	assertTrue("Should not be valid", entity.getProperty("toDateTime").isValid());

	assertEquals("Incorrect value", new DateTime(1999, 01, 01, 0, 0, 0, 0), entity.getFromDateTime());
	assertEquals("Incorrect value", new DateTime(2000, 01, 01, 0, 0, 0, 0), entity.getToDateTime());
    }

}
