package ua.com.fielden.platform.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.utils.DateUtils.max;
import static ua.com.fielden.platform.utils.DateUtils.min;

public class DateUtilsTest {
    private final Date earlierDate = new DateTime("2001-01-01").toDate();
    private final Date laterDate = new DateTime("2002-01-01").toDate();

    @Test
    public void min_returns_the_earier_of_two_non_null_dates() {
        assertEquals(earlierDate, min(earlierDate, laterDate));
        assertEquals(earlierDate, min(laterDate, earlierDate));
        assertEquals(earlierDate, min(earlierDate, earlierDate));
    }

    @Test
    public void min_returns_non_null_argument_if_one_of_arguments_is_null_and_null_if_both_are_null() {
        assertEquals(earlierDate, min(earlierDate, null));
        assertEquals(laterDate, min(null, laterDate));
        assertNull(min(null, null));
    }

    @Test
    public void max_returns_the_latre_of_two_non_null_dates() {
        assertEquals(laterDate, max(earlierDate, laterDate));
        assertEquals(laterDate, max(laterDate, earlierDate));
        assertEquals(laterDate, max(laterDate, laterDate));
    }

    @Test
    public void max_returns_non_null_argument_if_one_of_arguments_is_null_and_null_if_both_are_null() {
        assertEquals(earlierDate, max(earlierDate, null));
        assertEquals(laterDate, max(null, laterDate));
        assertNull(max(null, null));
    }

    @Test
    public void time_has_today_as_date_part() {
        final Date date = DateUtils.time(10, 35);

        final var dateToCheck = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        assertEquals(LocalDate.now(ZoneId.systemDefault()), dateToCheck);
    }

    @Test
    public void mergeDateAndTime_combines_the_date_and_time_from_two_dates() {
        final var dateWithDatePart = new DateTime(2001, 1, 1, 23, 56).toDate();
        final var dateWithTimePart = new DateTime(2025, 1, 1, 14, 45).toDate();
        final var date = DateUtils.mergeDateAndTime(dateWithDatePart, dateWithTimePart);
        assertEquals(new DateTime(2001, 1, 1, 14, 45).toDate(), date);
    }

    @Test
    public void diffHours_calculates_fractional_hours_in_absolute_terms_and_scale_2() {
        final var date1 = new DateTime(2025, 1, 1, 23, 10).toDate();
        final var date2 = new DateTime(2025, 1, 1, 13, 10).toDate();

        assertEquals(new BigDecimal("10.00"), DateUtils.diffHours(date1, date2));
        assertEquals(new BigDecimal("10.00"), DateUtils.diffHours(date2, date1));

        final var date3 = new DateTime(2025, 1, 1, 10, 0, 0).toDate();
        final var date4 = new DateTime(2025, 1, 1, 11, 15, 10).toDate();

        assertEquals(new BigDecimal("1.25"), DateUtils.diffHours(date3, date4));
        assertEquals(new BigDecimal("1.25"), DateUtils.diffHours(date4, date3));
    }

}
