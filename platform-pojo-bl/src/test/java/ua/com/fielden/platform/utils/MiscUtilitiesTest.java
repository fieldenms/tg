package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertEquals;

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

}
