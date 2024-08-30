package ua.com.fielden.platform.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void indexOfAnyBut() {
        assertEquals(-1, StringUtils.indexOfAnyBut("", 0, 0, 'a'));
        assertEquals(-1, StringUtils.indexOfAnyBut("a", 0, 0, 'a'));
        assertEquals(-1, StringUtils.indexOfAnyBut("a", 0, 1, 'a'));
        assertEquals(0, StringUtils.indexOfAnyBut("a", 0, 1));
        assertEquals(0, StringUtils.indexOfAnyBut("ab", 0, 2));
        assertEquals(1, StringUtils.indexOfAnyBut("ab", 0, 2, 'a'));
        assertEquals(-1, StringUtils.indexOfAnyBut("ab", 0, 2, 'a', 'b'));
        assertEquals(2, StringUtils.indexOfAnyBut("abcd", 0, 4, 'a', 'b'));
        assertEquals(2, StringUtils.indexOfAnyBut("abcd", 2, 4, 'a', 'b'));
        assertEquals(3, StringUtils.indexOfAnyBut("abcd", 3, 4, 'a', 'b'));
        assertEquals(2, StringUtils.indexOfAnyBut("abcd", 'a', 'b'));
        assertEquals(0, StringUtils.indexOfAnyBut("zabcd", 'a', 'b'));
        assertEquals(1, StringUtils.indexOfAnyBut("azabcd", 'a', 'b'));
    }

}
