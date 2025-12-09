package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithRangeProperties;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;

import java.util.Date;

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
    public void LeProperty_and_GeProperty_validators_for_time_only_date_periods_report_errors_without_the_date_components() {
        final var entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        final var fromTime1 = new DateTime("2025-09-07T16:30").toDate();
        final var toTime1 = new DateTime("2025-09-07T17:30").toDate();

        final MetaProperty<Date> mpFromDateTimeOnly = entity.getProperty("fromDateTimeOnly");
        final MetaProperty<Date> mpToDateTimeOnly = entity.getProperty("toDateTimeOnly");

        assertTrue(mpFromDateTimeOnly.isValid());
        assertTrue(mpToDateTimeOnly.isValid());

        entity.setFromDateTimeOnly(fromTime1);
        entity.setToDateTimeOnly(toTime1);

        assertTrue(mpFromDateTimeOnly.isValid());
        assertTrue(mpToDateTimeOnly.isValid());

        final var fromTime2 = new DateTime("2025-09-07T18:00").toDate();
        entity.setFromDateTimeOnly(fromTime2);
        assertFalse(mpFromDateTimeOnly.isValid());
        assertEquals("Property [From Date Time Only] (value [18:00]) cannot be after property [To Date Time Only] (value [17:30]).", mpFromDateTimeOnly.getFirstFailure().getMessage());

        final var toTime2 = new DateTime("2025-09-07T18:30").toDate();
        entity.setToDateTimeOnly(toTime2);
        assertTrue(mpFromDateTimeOnly.isValid());
        assertTrue(mpToDateTimeOnly.isValid());

        entity.setToDateTimeOnly(toTime1);
        assertFalse(mpToDateTimeOnly.isValid());
        assertEquals("Property [To Date Time Only] (value [17:30]) cannot be before property [From Date Time Only] (value [18:00]).", mpToDateTimeOnly.getFirstFailure().getMessage());
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

    @Test
    public void LeProperty_and_GeProperty_validators_for_date_only_date_periods_report_errors_without_the_time_components() {
        final var entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        final var fromDate1 = new DateTime("2025-09-07").toDate();
        final var toDate1 = new DateTime("2025-09-08").toDate();

        final MetaProperty<Date> mpFromDateDateOnly = entity.getProperty("fromDateDateOnly");
        final MetaProperty<Date> mpToDateDateOnly = entity.getProperty("toDateDateOnly");

        assertTrue(mpFromDateDateOnly.isValid());
        assertTrue(mpToDateDateOnly.isValid());

        entity.setFromDateDateOnly(fromDate1);
        entity.setToDateDateOnly(toDate1);

        assertTrue(mpFromDateDateOnly.isValid());
        assertTrue(mpToDateDateOnly.isValid());

        final var fromDate2 = new DateTime("2025-09-09").toDate();
        entity.setFromDateDateOnly(fromDate2);
        assertFalse(mpFromDateDateOnly.isValid());
        assertEquals("Property [From Date Date Only] (value [09/09/2025]) cannot be after property [To Date Date Only] (value [08/09/2025]).", mpFromDateDateOnly.getFirstFailure().getMessage());

        final var toDate2 = new DateTime("2025-09-10").toDate();
        entity.setToDateDateOnly(toDate2);
        assertTrue(mpFromDateDateOnly.isValid());
        assertTrue(mpToDateDateOnly.isValid());

        entity.setToDateDateOnly(toDate1);
        assertFalse(mpToDateDateOnly.isValid());
        assertEquals("Property [To Date Date Only] (value [08/09/2025]) cannot be before property [From Date Date Only] (value [09/09/2025]).", mpToDateDateOnly.getFirstFailure().getMessage());
    }

    @Test
    public void LeProperty_and_GeProperty_validators_for_date_and_time_periods_report_errors_with_both_the_date_and_time_components() {
        final var entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        final var fromDate1 = new DateTime("2025-09-07T13:00").toDate();
        final var toDate1 = new DateTime("2025-09-08T14:00").toDate();

        final MetaProperty<Date> mpFromDate = entity.getProperty("fromDate");
        final MetaProperty<Date> mpToDate = entity.getProperty("toDate");

        assertTrue(mpFromDate.isValid());
        assertTrue(mpToDate.isValid());

        entity.setFromDate(fromDate1);
        entity.setToDate(toDate1);

        assertTrue(mpFromDate.isValid());
        assertTrue(mpToDate.isValid());

        final var fromDate2 = new DateTime("2025-09-09T15:00:23.000").toDate();
        entity.setFromDate(fromDate2);
        assertFalse(mpFromDate.isValid());
        assertEquals("Property [From Date] (value [09/09/2025 15:00:23.000]) cannot be after property [To Date] (value [08/09/2025 14:00]).", mpFromDate.getFirstFailure().getMessage());

        final var toDate2 = new DateTime("2025-09-10T16:00").toDate();
        entity.setToDate(toDate2);
        assertTrue(mpFromDate.isValid());
        assertTrue(mpToDate.isValid());

        entity.setToDate(toDate1);
        assertFalse(mpToDate.isValid());
        assertEquals("Property [To Date] (value [08/09/2025 14:00]) cannot be before property [From Date] (value [09/09/2025 15:00:23.000]).", mpToDate.getFirstFailure().getMessage());
    }

    @Test
    public void strict_less_than_is_enforced_for_LeProperty_with_lt() {
        final var entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        final var fromDate1 = new DateTime("2025-09-07T13:00").toDate();
        final var toDate1 = new DateTime("2025-09-08T14:00").toDate();

        final MetaProperty<Date> mpFromDateStrict = entity.getProperty("fromDateStrict");
        final MetaProperty<Date> mpToDateStrict = entity.getProperty("toDateStrict");

        entity.setFromDateStrict(fromDate1);
        entity.setToDateStrict(toDate1);
        assertTrue(mpFromDateStrict.isValid());
        assertTrue(mpToDateStrict.isValid());

        entity.setFromDateStrict(toDate1);
        assertFalse(mpFromDateStrict.isValid());
        assertEquals("Property [From Date Strict] (value [08/09/2025 14:00]) cannot be after or equal to property [To Date Strict] (value [08/09/2025 14:00]).", mpFromDateStrict.getFirstFailure().getMessage());
    }

    @Test
    public void strict_greater_than_is_enforced_for_GeProperty_with_gt() {
        final var entity = factory.newByKey(EntityWithRangeProperties.class, "key");
        final var fromDate1 = new DateTime("2025-09-07T13:00").toDate();
        final var toDate1 = new DateTime("2025-09-08T14:00").toDate();

        final MetaProperty<Date> mpFromDateStrict = entity.getProperty("fromDateStrict");
        final MetaProperty<Date> mpToDateStrict = entity.getProperty("toDateStrict");

        entity.setFromDateStrict(fromDate1);
        entity.setToDateStrict(toDate1);
        assertTrue(mpFromDateStrict.isValid());
        assertTrue(mpToDateStrict.isValid());

        entity.setToDateStrict(fromDate1);
        assertFalse(mpToDateStrict.isValid());
        assertEquals("Property [To Date Strict] (value [07/09/2025 13:00]) cannot be before or equal to property [From Date Strict] (value [07/09/2025 13:00]).", mpToDateStrict.getFirstFailure().getMessage());
    }

}
