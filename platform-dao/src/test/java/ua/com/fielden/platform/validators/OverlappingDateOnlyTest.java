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
    public void new_period_overlapping_the_start_of_an_existing_period_overlaps() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));        
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void new_period_overlapping_the_end_of_an_existing_period_overlaps() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-20 00:00:00")).setToDateProp(date("2021-02-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void new_period_containing_an_existing_period_overlaps() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-02-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Jan 12 to Feb-01", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void new_period_inside_an_existing_period_overlaps() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-20 00:00:00")).setToDateProp(date("2021-01-25 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void new_period_ending_before_an_existing_period_starts_does_not_overlap() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-10 00:00:00"));
        assertFalse(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void new_period_ending_on_the_date_an_existing_period_starts_overlaps() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Jan 12 to Feb-01", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void new_period_ending_on_the_date_an_existing_period_starts_overlaps_regardless_of_time_portions() {
        save(new_(TgDateTestEntity.class, "2021 from Mar 01 at 13:01:12 to Mar 10 at 10:43:29").setFromDateProp(date("2021-03-02 13:01:12")).setToDateProp(date("2021-03-10 10:43:29")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-20 15:00:00")).setToDateProp(date("2021-03-02 10:13:17"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Mar 01 at 13:01:12 to Mar 10 at 10:43:29", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void new_period_starting_on_the_date_an_existing_period_ends_overlaps() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-01 00:00:00")).setToDateProp(date("2021-02-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Jan 12 to Feb-01", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void new_period_starting_on_the_date_an_existing_period_ends_overlaps_regardless_of_time_portions() {
        save(new_(TgDateTestEntity.class, "2021 from Jan 12 to Feb-01").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-01 08:15:00")).setToDateProp(date("2021-02-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2021 from Jan 12 to Feb-01", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

    @Test
    public void new_period_starting_the_day_after_an_existing_period_ends_does_not_overlap() {
        save(new_(TgDateTestEntity.class, "2020 from Jan 12 to Feb 01").setFromDateProp(date("2020-01-12 00:00:00")).setToDateProp(date("2020-02-01 00:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "2020 from Feb 02 to Feb 12").setFromDateProp(date("2020-02-02 00:00:00")).setToDateProp(date("2020-02-12 00:00:00"));
        assertFalse(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
    }

    @Test
    public void periods_meeting_on_the_same_date_at_different_times_overlap() {
        save(new_(TgDateTestEntity.class, "2020 from Jan 12 to Feb 01 at 13:00").setFromDateProp(date("2020-01-12 00:00:00")).setToDateProp(date("2020-02-01 13:00:00")));
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "2020 from Feb 01 at 14:00 to Feb 12").setFromDateProp(date("2020-02-01 14:00:00")).setToDateProp(date("2020-02-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, co, "fromDateProp", "toDateProp"));
        assertEquals("2020 from Jan 12 to Feb 01 at 13:00", Validators.findFirstOverlapping(dte, co, "fromDateProp", "toDateProp").getKey());
    }

}