package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.utils.MiscUtilities.substringFrom;

import java.io.InputStream;

import org.junit.Test;

/**
 * 
 * Test case for miscellaneous utilities.
 * 
 * @author TG Team
 * 
 */
public class MiscUtilitiesTest {

    private static final String SINGLE_LINE = "single line value";
    private static final String MULTI_LINE = "line 1\nline 2\nline 3";

    @Test
    public void test_single_line_string_to_input_stream_conversion() throws Exception {
        final InputStream ins = MiscUtilities.convertToInputStream(SINGLE_LINE);
        assertEquals("Incorrect value of the converted string.", SINGLE_LINE, MiscUtilities.convertToString(ins));
    }

    @Test
    public void test_multi_line_string_to_input_stream_conversion() throws Exception {
        final InputStream ins = MiscUtilities.convertToInputStream(MULTI_LINE);
        assertEquals("Incorrect value of the converted string.", MULTI_LINE, MiscUtilities.convertToString(ins));
    }

    @Test
    public void substringFrom_returns_a_substring_starting_from_the_first_occurence_of_a_given_string() {
        assertEquals("abcd", substringFrom("abcd", "a"));
        assertEquals("bcd", substringFrom("abcd", "b"));
        assertEquals("d", substringFrom("abcd", "d"));
        // starts from the first occurence of "b"
        assertEquals("bcdb", substringFrom("abcdb", "b"));
        // returns the same string when not found
        assertEquals("abcdb", substringFrom("abcdb", "z"));
        assertEquals("abcdb", substringFrom("abcdb", ""));
        assertEquals("", substringFrom("", "a"));
    }

}
