package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithRangeProperties;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.utils.DateUtils.compareDateOnly;
import static ua.com.fielden.platform.utils.DateUtils.compareTimeOnly;

/// A test case for validation of range properties.
///
public class DateRangePropertyValidatorTest {

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
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

    @Test
    public void time_only_dates_compare_by_time_component_only() {
        final var entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        final var earlierDateWithLaterTime = new DateTime(2025, 9, 7, 16, 30, 0, 0).toDate();
        final var laterDateWithEarlierTime = new DateTime(2025, 9, 8, 10, 30, 0, 0).toDate();

        assertTrue(earlierDateWithLaterTime.compareTo(laterDateWithEarlierTime) < 0);
        assertTrue(compareTimeOnly(laterDateWithEarlierTime, earlierDateWithLaterTime) < 0);
        assertTrue(compareDateOnly(earlierDateWithLaterTime, laterDateWithEarlierTime) < 0);

        entity.setFromDateTimeOnly(earlierDateWithLaterTime);
        entity.setToDateTimeOnly(laterDateWithEarlierTime);

        assertTrue(entity.getProperty("fromDateTimeOnly").isValid());
        assertFalse(entity.getProperty("toDateTimeOnly").isValid());

        entity.setFromDateTimeOnly(laterDateWithEarlierTime);
        entity.setToDateTimeOnly(earlierDateWithLaterTime);

        assertTrue(entity.getProperty("fromDateTimeOnly").isValid());
        assertTrue(entity.getProperty("toDateTimeOnly").isValid());

        assertEquals(laterDateWithEarlierTime, entity.getFromDateTimeOnly());
        assertEquals(earlierDateWithLaterTime, entity.getToDateTimeOnly());
    }

    @Test
    public void date_only_dates_compare_by_date_component_only() {
        final var entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        final var sameDateWithLaterTime = new DateTime(2025, 9, 7, 16, 30, 0, 0).toDate();
        final var sameDateWithEarlierTime = new DateTime(2025, 9, 7, 10, 30, 0, 0).toDate();

        assertTrue(sameDateWithEarlierTime.compareTo(sameDateWithLaterTime) < 0);
        assertTrue(compareDateOnly(sameDateWithEarlierTime, sameDateWithLaterTime) == 0);
        assertTrue(compareTimeOnly(sameDateWithEarlierTime, sameDateWithLaterTime) < 0);

        entity.setFromDateDateOnly(sameDateWithLaterTime);
        entity.setToDateDateOnly(sameDateWithEarlierTime);

        assertTrue(entity.getProperty("fromDateDateOnly").isValid());
        assertTrue(entity.getProperty("toDateDateOnly").isValid());
        assertEquals(sameDateWithLaterTime, entity.getFromDateDateOnly());
        assertEquals(sameDateWithEarlierTime, entity.getToDateDateOnly());

        entity.setFromDateDateOnly(sameDateWithEarlierTime);
        entity.setToDateDateOnly(sameDateWithLaterTime);

        assertTrue(entity.getProperty("fromDateDateOnly").isValid());
        assertTrue(entity.getProperty("toDateDateOnly").isValid());

        assertEquals(sameDateWithEarlierTime, entity.getFromDateDateOnly());
        assertEquals(sameDateWithLaterTime, entity.getToDateDateOnly());
    }

}
