package ua.com.fielden.platform.entity_centre.mnemonics;

import static org.junit.Assert.assertEquals;

import java.util.Date;

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

}