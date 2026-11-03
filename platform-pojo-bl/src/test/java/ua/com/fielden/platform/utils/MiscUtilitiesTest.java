package ua.com.fielden.platform.utils;

import org.junit.Test;

import java.io.InputStream;
import java.util.MissingFormatArgumentException;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.utils.MiscUtilities.pathWith;
import static ua.com.fielden.platform.utils.MiscUtilities.uriPath;

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
    public void stringFormatter_returns_a_function_that_formats_a_supplied_string() {
        final Function<String, String> formatter = MiscUtilities.stringFormatter(123);
        assertEquals("Incorrectly formatted string.", "The number is 123.", formatter.apply("The number is %s."));

        // the number of special format characters should be equal to the number of arguments given to stringFormatter()
        assertThrows("Incorrect number of special format characters should produce an exception.",
                MissingFormatArgumentException.class, () -> formatter.apply("The numbers are %s and %s."));
    }

    @Test
    public void uriPath_with_a_single_component_returns_that_component() {
        assertEquals("/a/", uriPath("/a/"));
    }

    @Test
    public void uriPath_separates_components_with_a_slash() {
        assertEquals("a/b/c", uriPath("a", "b", "c"));
    }

    @Test
    public void uriPath_does_not_duplicate_a_slash_that_ends_a_component() {
        assertEquals("a/b/c", uriPath("a/", "b/", "c"));
    }

    @Test
    public void uriPath_does_not_duplicate_a_slash_that_starts_a_component() {
        assertEquals("a/b/c", uriPath("a", "/b", "/c"));
    }

    @Test
    public void uriPath_does_not_duplicate_slashes_that_end_and_start_adjacent_components() {
        assertEquals("a/b/c", uriPath("a/", "/b/", "/c"));
    }

    @Test
    public void uriPath_preserves_slashes_inside_components_and_at_the_ends_of_the_path() {
        assertEquals("https://app.com/mcp", uriPath("https://app.com", "/mcp"));
        assertEquals("/.well-known/oauth-protected-resource/mcp/", uriPath("/.well-known/oauth-protected-resource", "mcp/"));
    }

    @Test
    public void pathWith_separates_components_with_a_multi_character_separator() {
        assertEquals("a::b::c", pathWith("::", "a", "b", "c"));
    }

    @Test
    public void pathWith_does_not_duplicate_a_multi_character_separator_that_ends_or_starts_a_component() {
        assertEquals("a::b::c::d", pathWith("::", "a::", "b", "::c::", "::d"));
    }

    @Test
    public void pathWith_adds_a_multi_character_separator_after_a_component_ending_with_its_part() {
        assertEquals("a:::b", pathWith("::", "a:", "b"));
    }

}
