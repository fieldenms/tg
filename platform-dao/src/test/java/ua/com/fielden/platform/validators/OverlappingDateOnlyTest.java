package ua.com.fielden.platform.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.ITgDateTestEntity;
import ua.com.fielden.platform.sample.domain.TgDateTestEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Validators;

/**
 * A test case for to the validation logic that determins overlapping of time periods for cases where both the start and end properties as {@code @DateOnly}.
 *
 * @author TG Team
 *
 */
public class OverlappingDateOnlyTest extends AbstractDaoTestCase {

    private final ITgDateTestEntity co = co(TgDateTestEntity.class);

    @Test
    public void validator_detects_intersection_at_the_beginning_of_the_test_interval() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));        
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_intersection_at_the_end_of_the_test_iterval() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-20 00:00:00")).setToDateProp(date("2021-02-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_interval_that_falls_into_the_test_interval() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-02-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Jan 12 to Feb-01", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void validator_detects_interval_that_incompases_the_test_interval() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-20 00:00:00")).setToDateProp(date("2021-01-25 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_non_overlapping_intrevals_as_non_overlapping() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-10 00:00:00"));
        assertFalse(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_interval_that_starts_on_the_same_date_as_the_end_of_the_test_interval() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Jan 12 to Feb-01", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void validator_detects_interval_that_starts_on_the_same_date_as_the_end_of_the_test_interval_with_time_portion() {
        save(new_(TgDateTestEntity.class, "2021 from Mar 01 at 13:01:12 to Mar 10 at 10:43:29").setFromDateProp(date("2021-03-02 13:01:12")).setToDateProp(date("2021-03-10 10:43:29")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-20 15:00:00")).setToDateProp(date("2021-03-02 10:13:17"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Mar 01 at 13:01:12 to Mar 10 at 10:43:29", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void validator_detects_interval_that_ends_on_the_same_date_as_the_start_of_the_test_interval() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-01 00:00:00")).setToDateProp(date("2021-02-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Jan 12 to Feb-01", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void validator_detects_interval_that_ends_on_the_same_date_as_the_start_of_the_test_interval_with_time_portion() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-01 08:15:00")).setToDateProp(date("2021-02-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Jan 12 to Feb-01", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void sequential_periods_can_touch() {
        save(new_(TgDateTestEntity.class, "2020 from Jan 12 to Feb 01").setFromDateProp(date("2020-01-12 00:00:00")).setToDateProp(date("2020-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "2020 from Feb 02 to Feb 12").setFromDateProp(date("2020-02-02 00:00:00")).setToDateProp(date("2020-02-12 00:00:00"));
        assertFalse(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void periods_that_do_not_overlap_in_time_portions_but_overlap_in_date_portions_are_recognised_as_overlapping_for_dateonly_case() {
        save(new_(TgDateTestEntity.class, "2020 from Jan 12 to Feb 01 at 13:00").setFromDateProp(date("2020-01-12 00:00:00")).setToDateProp(date("2020-02-01 13:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "2020 from Feb 01 at 14:00 to Feb 12").setFromDateProp(date("2020-02-01 14:00:00")).setToDateProp(date("2020-02-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2020 from Jan 12 to Feb 01 at 13:00", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

}