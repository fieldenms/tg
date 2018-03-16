package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.utils.DateUtils.max;
import static ua.com.fielden.platform.utils.DateUtils.min;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

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

}
