package ua.com.fielden.platform.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.utils.ArrayUtils.*;

public class ArrayUtilsTest {

    @Test
    public void prepend_produces_array_with_one_extra_element_as_the_first_element() {
        final String[] array = new String[] {"b", "c"};
        final String[] newArray = ArrayUtils.prepend("a", array);
        assertThat(newArray.length).isEqualTo(array.length + 1);
        assertThat(newArray[0]).isEqualTo("a");

        final String[] emptyArray = new String[] {};
        final String[] newArray1 = ArrayUtils.prepend("a", emptyArray);
        assertThat(newArray1.length).isEqualTo(emptyArray.length + 1);
        assertThat(newArray1[0]).isEqualTo("a");
    }

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

    @Test
    public void findIndex_returns_index_of_the_first_element_that_satisfies_predicate() {
        assertEquals(1, findIndex(new Integer[]{ 1, 2 }, x -> x > 1));
        assertEquals(1, findIndex(new Integer[]{ 1, 2, 3 }, x -> x > 1));
        assertEquals(0, findIndex(new Integer[]{ 2 }, x -> x > 1));
        assertEquals(-1, findIndex(new Integer[]{ 1 }, x -> x > 1));
        assertEquals(-1, findIndex(new Integer[]{}, x -> x > 1));
        assertEquals(-1, findIndex(new Integer[]{ -1, -2, -3 }, x -> x > 1));
    }

}
