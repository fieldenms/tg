package ua.com.fielden.platform.utils;

import org.apache.xpath.operations.String;
import org.junit.Test;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.utils.ArrayUtils.contains;
import static ua.com.fielden.platform.utils.ArrayUtils.getLast;

public class ArrayUtilsTest {
    
    @Test
    public void getLast_fails_if_array_is_empty() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> getLast(new String[] {}));
    }

    @Test
    public void getLast_returns_the_last_element_of_non_empty_array() {
        assertEquals(1, getLast(new Object[] {1}));
        assertEquals(2, getLast(new Object[] {1, 2}));
        assertEquals(3, getLast(new Object[] {1, 2, 3}));
        assertEquals(1, getLast(new Object[] {1, 2, 1}));
        assertNull(getLast(new Object[]{null}));
        assertNull(getLast(new Object[]{"a", null}));
    }

    @Test
    public void contains_returns_false_for_empty_array() {
        assertFalse(contains(new Object[]{}, "a"));
        assertFalse(contains(new Object[]{}, null));
    }

    @Test
    public void contains_returns_true_if_array_contains_item() {
        assertTrue(contains(new Object[]{"a"}, "a"));
        assertTrue(contains(new Object[]{"a", null}, "a"));
        assertTrue(contains(new Object[]{"a", null}, null));
        assertTrue(contains(new Object[]{"a", "b", "a"}, "a"));
    }

}
