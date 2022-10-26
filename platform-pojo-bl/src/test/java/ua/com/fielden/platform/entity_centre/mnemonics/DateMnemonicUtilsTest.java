package ua.com.fielden.platform.entity_centre.mnemonics;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Random;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.utils.DefaultDates;

/**
 * A test case for {@link DateMnemonicUtils}.
 *
 * @author TG Team
 *
 */
public class DateMnemonicUtilsTest {
    private static final Date PREV_SUNDAY = new DateTime(2022, 10, 23, 00, 00).toDate();
    private static final Date MONDAY = new DateTime(2022, 10, 24, 00, 00).toDate();
    private static final Date TUESDAY = new DateTime(2022, 10, 25, 00, 00).toDate();
    private static final Date WEDNESDAY = new DateTime(2022, 10, 26, 00, 00).toDate();
    private static final Date THURSDAY = new DateTime(2022, 10, 27, 00, 00).toDate();
    private static final Date FRIDAY = new DateTime(2022, 10, 28, 00, 00).toDate();
    private static final Date SATRUDAY = new DateTime(2022, 10, 29, 00, 00).toDate();
    private static final Date SUNDAY = new DateTime(2022, 10, 30, 00, 00).toDate();
    
    @Test
    public void week_starts_Monday_thus_Monday_is_start_of_week_for_all_other_week_days() {
        final DefaultDates dates = new DefaultDates(false, 1 /* Monday */, 1, 7);
        assertEquals(MONDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(MONDAY, MnemonicEnum.WEEK, dates));
        assertEquals(MONDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(TUESDAY, MnemonicEnum.WEEK, dates));
        assertEquals(MONDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(WEDNESDAY, MnemonicEnum.WEEK, dates));
        assertEquals(MONDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(THURSDAY, MnemonicEnum.WEEK, dates));
        assertEquals(MONDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(FRIDAY, MnemonicEnum.WEEK, dates));
        assertEquals(MONDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(SATRUDAY, MnemonicEnum.WEEK, dates));
        assertEquals(MONDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(SUNDAY, MnemonicEnum.WEEK, dates));
    }

    @Test
    public void week_starts_Sunday_thus_Sunday_is_start_of_week_for_all_other_week_days() {
        final DefaultDates dates = new DefaultDates(false, 7 /* Sunday */, 1, 7);
        assertEquals(PREV_SUNDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(PREV_SUNDAY, MnemonicEnum.WEEK, dates));
        assertEquals(PREV_SUNDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(MONDAY, MnemonicEnum.WEEK, dates));
        assertEquals(PREV_SUNDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(TUESDAY, MnemonicEnum.WEEK, dates));
        assertEquals(PREV_SUNDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(WEDNESDAY, MnemonicEnum.WEEK, dates));
        assertEquals(PREV_SUNDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(THURSDAY, MnemonicEnum.WEEK, dates));
        assertEquals(PREV_SUNDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(FRIDAY, MnemonicEnum.WEEK, dates));
        assertEquals(PREV_SUNDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(SATRUDAY, MnemonicEnum.WEEK, dates));
        // SUNDAY is actually the start of the next week, so SUNDAY is the expected date
        assertEquals(SUNDAY, DateMnemonicUtils.startOfDateRangeThatIncludes(SUNDAY, MnemonicEnum.WEEK, dates));
    }

    @Test
    public void FY_starts_on_1st_of_July_thus_for_any_date_in_that_FY_it_is_the_start() {
        final DefaultDates dates = new DefaultDates(false, 1 , 1, 7); // 1st of July
        final DateTime startOfFinYear = new DateTime(2022, 07, 01, 00, 00); // 1st of July, 2022
        // let's generate all days between 2022-07-01 and 2023-06-30, and assert that 2022-07-01 is returned for all of them
        Stream.iterate(startOfFinYear, date -> date.plusDays(1)).limit(365).forEach( date -> {
            assertEquals(startOfFinYear.toDate(), DateMnemonicUtils.startOfDateRangeThatIncludes(date.toDate(), MnemonicEnum.FIN_YEAR, dates));
        });
    }

    @Test
    public void FY_starts_on_6st_of_April_thus_for_any_date_in_that_FY_it_is_the_start() {
        final DefaultDates dates = new DefaultDates(false, 1 , 6, 4); // 6st of April
        final DateTime startOfFinYear = new DateTime(2022, 04, 06, 00, 00); // 6th of April, 2022
        // let's generate all days between 2022-04-06 and 2023-04-5, and assert that 2022-04-06 is returned for all of them
        Stream.iterate(startOfFinYear, date -> date.plusDays(1)).limit(365).forEach( date -> {
            assertEquals(startOfFinYear.toDate(), DateMnemonicUtils.startOfDateRangeThatIncludes(date.toDate(), MnemonicEnum.FIN_YEAR, dates));
        });
    }

    @Test
    public void FY_starts_on_last_day_of_February_thus_for_any_date_in_that_FY_it_is_the_start() {
        final DefaultDates dates = new DefaultDates(false, 1 , 31, 2); // last day of February
        final DateTime startOfFinYear = new DateTime(2022, 02, 28, 00, 00); // the actual last day of February, 2022
        // let's generate all days between 2022-02-28 and 2023-02-27, and assert that 2022-02-28 is returned for all of them
        Stream.iterate(startOfFinYear, date -> date.plusDays(1)).limit(365).forEach( date -> {
            assertEquals(startOfFinYear.toDate(), DateMnemonicUtils.startOfDateRangeThatIncludes(date.toDate(), MnemonicEnum.FIN_YEAR, dates));
        });
    }

    @Test
    public void FY_starts_on_last_day_of_June_thus_for_any_date_in_that_FY_it_is_the_start() {
        final DefaultDates dates = new DefaultDates(false, 1 , 31, 6); // last day of June
        final DateTime startOfFinYear = new DateTime(2022, 06, 30, 00, 00); // the actual last day of June, 2022
        // let's generate all days between 2022-06-30 and 2023-06-29, and assert that 2022-06-30 is returned for all of them
        Stream.iterate(startOfFinYear, date -> date.plusDays(1)).limit(365).forEach( date -> {
            assertEquals(startOfFinYear.toDate(), DateMnemonicUtils.startOfDateRangeThatIncludes(date.toDate(), MnemonicEnum.FIN_YEAR, dates));
        });
    }

}