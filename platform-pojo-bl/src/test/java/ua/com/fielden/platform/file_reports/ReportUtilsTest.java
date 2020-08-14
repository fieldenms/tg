package ua.com.fielden.platform.file_reports;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReportUtilsTest {

    @Test
    public void strings_are_properly_sanitised_for_Jasper_reports() {
        final String dirty = "\u202F\t\n Dirty\t\u202F\t\tstring\u00A0\nwith\u205Fweird\u2007\u3000\rspaces \t\r  \n  \r\u3000";
        final String clean = ReportUtils.sanitizeString(dirty);
        final String expected = "Dirty string \nwith weird \rspaces";
        assertEquals(expected, clean);
    }

    @Test
    public void nulls_are_sanitised_to_empty_strings_without_NPE() {
        final String dirty = null;
        final String clean = ReportUtils.sanitizeString(dirty);
        final String expected = "";
        assertEquals(expected, clean);
    }

}
