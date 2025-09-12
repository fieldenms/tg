package ua.com.fielden.platform.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.utils.DateUtils.*;

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

    @Test
    public void isSameDay_can_be_used_to_check_isToday() {
        final var today = new DateTime(2025, 9, 8, 14, 2).toDate();
        final var dt1 = new DateTime(2025, 1, 1, 23, 10).toDate();
        final var dt2 = new DateTime(2025, 9, 8, 10, 15).toDate();
        assertFalse(DateUtils.isSameDay(dt1, today));
        assertTrue((DateUtils.isSameDay(dt2, today)));
    }

    @Test
    public void compareTimeOnly_returns_a_positive_number_if_the_first_date_has_later_time() {
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 12, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isPositive();
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 12, 0).toDate(), new DateTime(2025, 8, 9, 10, 0).toDate())).isPositive();
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 12, 0).toDate(), new DateTime(2025, 9, 8, 10, 0).toDate())).isPositive();
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 12, 0).toDate(), new DateTime(2024, 9, 9, 10, 0).toDate())).isPositive();
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 12, 0).toDate(), new DateTime(2026, 9, 9, 10, 0).toDate())).isPositive();
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 12, 0).toDate(), new DateTime(2025, 10, 10, 10, 0).toDate())).isPositive();
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 12, 0).toDate(), new DateTime(2025, 9, 9, 10, 30).toDate())).isPositive();
    }

    @Test
    public void compareTimeOnly_returns_a_negative_number_if_the_first_date_has_earlier_time() {
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 12, 0).toDate())).isNegative();
        assertThat(compareTimeOnly(new DateTime(2025, 8, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 12, 0).toDate())).isNegative();
        assertThat(compareTimeOnly(new DateTime(2025, 9, 8, 10, 0).toDate(), new DateTime(2025, 9, 9, 12, 0).toDate())).isNegative();
        assertThat(compareTimeOnly(new DateTime(2024, 9, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 12, 0).toDate())).isNegative();
        assertThat(compareTimeOnly(new DateTime(2026, 9, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 12, 0).toDate())).isNegative();
        assertThat(compareTimeOnly(new DateTime(2025, 10, 10, 10, 0).toDate(), new DateTime(2025, 9, 9, 12, 0).toDate())).isNegative();
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 10, 30).toDate(), new DateTime(2025, 9, 9, 12, 0).toDate())).isNegative();
    }

    @Test
    public void compareTimeOnly_returns_zero_if_both_dates_have_equal_time() {
        assertThat(compareTimeOnly(new DateTime(2025, 9, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isZero();
        assertThat(compareTimeOnly(new DateTime(2025, 10, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isZero();
        assertThat(compareTimeOnly(new DateTime(2025, 8, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isZero();
        assertThat(compareTimeOnly(new DateTime(2024, 9, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isZero();
        assertThat(compareTimeOnly(new DateTime(2026, 9, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isZero();
    }

    @Test
    public void compareDateOnly_returns_a_positive_number_if_the_first_date_has_later_date() {
        assertThat(compareDateOnly(new DateTime(2026, 9, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isPositive();
        assertThat(compareDateOnly(new DateTime(2025, 10, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isPositive();
        assertThat(compareDateOnly(new DateTime(2025, 9, 10, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isPositive();
        assertThat(compareDateOnly(new DateTime(2025, 10, 10, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isPositive();
        assertThat(compareDateOnly(new DateTime(2026, 10, 10, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isPositive();
        assertThat(compareDateOnly(new DateTime(2025, 10, 10, 2, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isPositive();
    }

    @Test
    public void compareDateOnly_returns_a_negative_number_if_the_first_date_has_earlier_date() {
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 10, 0).toDate(), new DateTime(2026, 9, 9, 10, 0).toDate())).isNegative();
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 10, 0).toDate(), new DateTime(2025, 10, 9, 10, 0).toDate())).isNegative();
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 10, 0).toDate(), new DateTime(2025, 9, 10, 10, 0).toDate())).isNegative();
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 10, 0).toDate(), new DateTime(2025, 10, 10, 10, 0).toDate())).isNegative();
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 10, 0).toDate(), new DateTime(2026, 10, 10, 10, 0).toDate())).isNegative();
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 10, 0).toDate(), new DateTime(2025, 10, 10, 2, 0).toDate())).isNegative();
    }

    @Test
    public void compareDateOnly_returns_zero_if_both_dates_have_equal_date() {
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 10, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isZero();
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 9, 0).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isZero();
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 10, 9).toDate(), new DateTime(2025, 9, 9, 10, 0).toDate())).isZero();
        assertThat(compareDateOnly(new DateTime(2025, 9, 9, 1, 2).toDate(), new DateTime(2025, 9, 9, 3, 4).toDate())).isZero();
    }

}
