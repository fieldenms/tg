package ua.com.fielden.platform.cypher;

import org.junit.Test;

import static org.junit.Assert.*;

public class LogReferenceTest {

    @Test
    public void reference_is_the_same_for_the_same_value_and_differs_for_different_values() {
        assertEquals(LogReference.of("session-identifier-1"), LogReference.of("session-identifier-1"));
        assertNotEquals(LogReference.of("session-identifier-1"), LogReference.of("session-identifier-2"));
    }

    @Test
    public void reference_does_not_disclose_the_value() {
        final String value = "0123456789abcdef0123456789abcdef";
        final String ref = LogReference.of(value);
        assertEquals(LogReference.LENGTH, ref.length());
        assertTrue(ref.matches("[0-9a-fA-F]+"));
        assertFalse(value.contains(ref));
    }

    @Test
    public void reference_of_a_short_value_has_the_same_length() {
        assertEquals(LogReference.LENGTH, LogReference.of("a").length());
    }

    @Test
    public void absent_value_has_a_reference_that_says_so() {
        assertEquals(LogReference.NONE, LogReference.of(null));
        assertEquals(LogReference.NONE, LogReference.of(""));
    }

}
