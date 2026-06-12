package ua.com.fielden.platform.test_config;

import org.joda.time.DateTime;
import org.junit.Test;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateParsingTest extends AbstractDaoTestCase {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Test
    public void date_parses_all_supported_string_formats() {
        assertSameDateTime("2024-01-25 00:00:00.000", date("2024-01-25"));
        assertSameDateTime("2024-01-25 12:35:00.000", date("2024-01-25 12:35"));
        assertSameDateTime("2024-01-25 12:35:45.000", date("2024-01-25 12:35:45"));
        assertSameDateTime("2024-01-25 12:35:45.678", date("2024-01-25 12:35:45.678"));
    }

    @Test
    public void dateTime_parses_all_supported_string_formats() {
        assertSameDateTime("2024-01-25 00:00:00.000", dateTime("2024-01-25"));
        assertSameDateTime("2024-01-25 12:35:00.000", dateTime("2024-01-25 12:35"));
        assertSameDateTime("2024-01-25 12:35:45.000", dateTime("2024-01-25 12:35:45"));
        assertSameDateTime("2024-01-25 12:35:45.678", dateTime("2024-01-25 12:35:45.678"));
    }

    @Override
    public boolean useSavedDataPopulationScript()   {return false;}

    @Override
    public boolean saveDataPopulationScriptToFile() {return false;}

    @Override
    protected void populateDomain() {}

    private void assertSameDateTime(final String expected, final Date actual) {
        assertEquals(expected, formatter.format(actual.toInstant().atZone(ZoneId.systemDefault())));
    }

    private void assertSameDateTime(final String expected, final DateTime dateTime) {
        assertSameDateTime(expected, dateTime.toDate());
    }

}
