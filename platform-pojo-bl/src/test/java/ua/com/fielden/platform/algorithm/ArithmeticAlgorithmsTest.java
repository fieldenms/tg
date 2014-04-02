package ua.com.fielden.platform.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.algorithm.ArithmeticAlgorithms.areValuesEqual;
import static ua.com.fielden.platform.algorithm.ArithmeticAlgorithms.lcm;
import static ua.com.fielden.platform.algorithm.ArithmeticAlgorithms.minIndex;

import org.junit.Test;

/**
 * A test case of all arithmetic algorithms.
 * 
 * @author TG Team
 * 
 */
public class ArithmeticAlgorithmsTest {

    @Test
    public void test_that_can_find_min_index() {
        assertEquals("Incorrect index of the minimum element.", new Integer(3), minIndex(2, 1, 1, 0, 2, 0));
        assertEquals("Incorrect index of the minimum element.", new Integer(0), minIndex(-1, 1, 1, 0, 2, 0));
        assertEquals("Incorrect index of the minimum element.", new Integer(5), minIndex(3, 1, 1, 5, 2, 0));
    }

    @Test
    public void test_that_min_index_behavious_correctly_under_when_null_is_passed() {
        try {
            minIndex(null);
            fail("An IllegalArgumentException is expected.");
        } catch (final IllegalArgumentException ex) {

        }
        try {
            minIndex(null, null, null);
            fail("An IllegalArgumentException is expected.");
        } catch (final IllegalArgumentException ex) {

        }
    }

    @Test
    public void test_that_can_identify_arrays_with_all_equal_values() {
        assertTrue("Failed to identify equality of all values in the array.", areValuesEqual(1, 1, 1));
        assertTrue("Failed to identify equality of all values in the array.", areValuesEqual(null, null, null));
        assertTrue("Failed to identify equality of all values in the array.", areValuesEqual(1));
    }

    @Test
    public void test_that_can_identify_arrays_with_not_all_equal_values() {
        assertFalse("Failed to identify inequality of all values in the array.", areValuesEqual(null, null, 1));
        assertFalse("Failed to identify inequality of all values in the array.", areValuesEqual(1, 1, null));
    }

    @Test
    public void test_lcm() {
        assertEquals("Incorrect LCM.", new Integer(60), lcm(10, 20, 60));
        assertEquals("Incorrect LCM.", new Integer(100), lcm(10, 20, 50));
        assertEquals("Incorrect LCM.", new Integer(10), lcm(10, 10, 10));
        assertEquals("Incorrect LCM.", new Integer(700), lcm(10, 20, 50, 70));
    }

}
