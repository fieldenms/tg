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
public class DateRangePropertyValidatorTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_date_range_validation_where_only_from_is_set() {
        final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        entity.setFromDate(new DateTime(2000, 01, 01, 0, 0, 0, 0).toDate());
        assertTrue("Should be valid", entity.getProperty("fromDate").isValid());
        assertTrue("Should be valid", entity.getProperty("toDate").isValid());
        assertEquals("Incorrect value", new DateTime(2000, 01, 01, 0, 0, 0, 0).toDate(), entity.getFromDate());
    }

    @Test
    public void test_date_range_validation_where_only_to_is_set() {
        final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        entity.setToDate(new DateTime(2000, 01, 01, 0, 0, 0, 0).toDate());
        assertTrue("Should be valid", entity.getProperty("fromDate").isValid());
        assertFalse("Should not be valid", entity.getProperty("toDate").isValid());
    }

    @Test
    public void test_date_range_validation_where_both_from_and_to_are_set_in_the_right_order() {
        final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        entity.setFromDate(new DateTime(2000, 01, 01, 0, 0, 0, 0).toDate());
        entity.setToDate(new DateTime(2000, 02, 01, 0, 0, 0, 0).toDate());
        assertTrue("Should be valid", entity.getProperty("fromDate").isValid());
        assertTrue("Should not be valid", entity.getProperty("toDate").isValid());
        assertEquals("Incorrect value", new DateTime(2000, 01, 01, 0, 0, 0, 0).toDate(), entity.getFromDate());
        assertEquals("Incorrect value", new DateTime(2000, 02, 01, 0, 0, 0, 0).toDate(), entity.getToDate());
    }

    @Test
    public void test_date_range_validation_where_range_is_set_incorrectly_with_error_recovery() {
        final EntityWithRangeProperties entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        entity.setFromDate(new DateTime(2000, 02, 01, 0, 0, 0, 0).toDate());
        entity.setToDate(new DateTime(2000, 01, 01, 0, 0, 0, 0).toDate());

        assertTrue("Should be valid", entity.getProperty("fromDate").isValid());
        assertEquals("Incorrect value", new DateTime(2000, 02, 01, 0, 0, 0, 0).toDate(), entity.getFromDate());
        assertFalse("Should not be valid", entity.getProperty("toDate").isValid());
        assertNull("Incorrect value", entity.getToDate());

        entity.setFromDate(new DateTime(1999, 01, 01, 0, 0, 0, 0).toDate());

        assertTrue("Should be valid", entity.getProperty("fromDate").isValid());
        assertTrue("Should not be valid", entity.getProperty("toDate").isValid());

        assertEquals("Incorrect value", new DateTime(1999, 01, 01, 0, 0, 0, 0).toDate(), entity.getFromDate());
        assertEquals("Incorrect value", new DateTime(2000, 01, 01, 0, 0, 0, 0).toDate(), entity.getToDate());
    }

}
