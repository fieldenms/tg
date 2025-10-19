package ua.com.fielden.platform.keygen;

import org.junit.Test;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.*;

/// Ensures correct generation of named numbers that are used for automatic entity key generations such as
/// work order and purchase order numbers.
///
public class KeyNumberTest extends AbstractDaoTestCase {

    public static final String KEY_RADIX_10 = "KEY_RADIX_10";
    public static final String KEY_RADIX_36 = "KEY_RADIX_36";

    private final IKeyNumber coKeyNumber = getInstance(IKeyNumber.class);

    @Test
    public void new_keynumbers_can_be_created_and_persisted_adhoc() {
        final String newKey = "YET-NON-EXISTING";
        try {
            coKeyNumber.currNumber(newKey);
            fail("Getting the next number for non existing key should fail.");
        } catch (final EntityCompanionException ex) {
            assertEquals("No number associated with key [%s].".formatted(newKey), ex.getMessage());
        }
        
        final Integer expected = 1;
        assertEquals("Incorrect initial number.", expected, coKeyNumber.nextNumber(newKey));
        assertEquals("Incorrect current number.", expected, coKeyNumber.currNumber(newKey));
    }
    
    @Test
    public void existing_keynumber_value_is_retrievable_by_key() {
        assertEquals("Incorrect current number.", Integer.valueOf(500), coKeyNumber.currNumber(KEY_RADIX_10));
    }

    @Test
    public void nextNumber_returns_the_next_keynumber_value_and_simultaneously_persists_it() {
        final Integer nextNumber = 501;
        assertEquals("Incorrectly generated next number.", nextNumber, coKeyNumber.nextNumber(KEY_RADIX_10));
        assertEquals("Incorrect current number after generating the next number.", nextNumber, coKeyNumber.currNumber(KEY_RADIX_10));
    }

    @Test
    public void nextNumbers_with_count_1_is_equivalent_to_nextNumber() {
        final Integer nextNumber = 501;
        final SortedSet<Integer> numbers = coKeyNumber.nextNumbers(KEY_RADIX_10, 1);
        assertEquals("Unexpected number of generated values.", 1, numbers.size());
        assertEquals("Incorrectly generated next number.", nextNumber, numbers.first());
        assertEquals("Incorrect current number after generating the next number.", nextNumber, coKeyNumber.currNumber(KEY_RADIX_10));
    }

    @Test
    public void nextNumbers_generated_the_requested_number_of_key_values_as_sorted_set_and_persists_the_last_one() {
        final SortedSet<Integer> expectedNumbers = new TreeSet<Integer>();
        expectedNumbers.add(501);
        expectedNumbers.add(502);
        expectedNumbers.add(503);
        expectedNumbers.add(504);
        expectedNumbers.add(505);
        final SortedSet<Integer> numbers = coKeyNumber.nextNumbers(KEY_RADIX_10, 5);
        assertEquals("Unexpected number of generated values.", 5, numbers.size());
        assertEquals(expectedNumbers, numbers);
        assertEquals("Incorrect current number after generating the next number.", numbers.last(), coKeyNumber.currNumber(KEY_RADIX_10));
    }

    @Test
    public void nextNumbers_for_the_count_of_less_than_1_returns_an_empty_set() {
        assertTrue("Empty set is expected.", coKeyNumber.nextNumbers(KEY_RADIX_10, 0).isEmpty());
        assertTrue("Empty set is expected.", coKeyNumber.nextNumbers(KEY_RADIX_10, -1).isEmpty());
    }

    @Test
    public void radix_outside_the_boundaries_throws_exception() {
        try {
            coKeyNumber.currNumber(KEY_RADIX_36, 37);
            fail();
        } catch (final NumberFormatException ex) {
            assertEquals("radix 37 greater than Character.MAX_RADIX", ex.getMessage());
        }
        try {
            coKeyNumber.nextNumber(KEY_RADIX_36, 1);
            fail();
        } catch (final NumberFormatException ex) {
            assertEquals("radix 1 less than Character.MIN_RADIX", ex.getMessage());
        }
        try {
            coKeyNumber.nextNumbers(KEY_RADIX_36, 3, 42);
            fail();
        } catch (final NumberFormatException ex) {
            assertEquals("radix 42 greater than Character.MAX_RADIX", ex.getMessage());
        }
    }

    @Test
    public void existing_keynumber_value_with_radix_36_is_retrievable_by_key() {
        assertEquals(Integer.valueOf("9Z", 36), coKeyNumber.currNumber(KEY_RADIX_36, 36));
    }

    @Test
    public void currNumber_with_radix_36_returns_value_with_radix_36() {
        assertEquals(Integer.valueOf("9Z", 36), coKeyNumber.currNumber(KEY_RADIX_36, 36));
    }

    @Test
    public void nextNumber_with_radix_36_updates_the_persisted_value_with_radix_36() {
        final Integer nextNumber = Integer.valueOf("A0", 36);
        assertEquals(nextNumber, coKeyNumber.nextNumber(KEY_RADIX_36, 36));
        assertEquals(nextNumber, coKeyNumber.currNumber(KEY_RADIX_36, 36));
    }

    @Test
    public void nextNumbers_with_radix_36_and_count_1_is_equivalent_to_nextNumber_with_radix_36() {
        final Integer nextNumber = Integer.valueOf("A0", 36);
        final SortedSet<Integer> numbers = coKeyNumber.nextNumbers(KEY_RADIX_36, 1, 36);
        assertEquals(1, numbers.size());
        assertEquals(nextNumber, numbers.first());
        assertEquals(nextNumber, coKeyNumber.currNumber(KEY_RADIX_36, 36));
    }


    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(KeyNumber.class, KEY_RADIX_10).setValue("500"));
        save(new_(KeyNumber.class, KEY_RADIX_36).setValue("9Z"));
    }

}
