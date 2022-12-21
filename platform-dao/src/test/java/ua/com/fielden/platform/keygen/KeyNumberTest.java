package ua.com.fielden.platform.keygen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * Ensures correct generation of named numbers that are used for automatic entity key generations such as 
 * in case of work orders and purchase orders.
 * 
 * @author TG Team
 * 
 */
public class KeyNumberTest extends AbstractDaoTestCase {
    private final IKeyNumber coKeyNumber = getInstance(IKeyNumber.class);

    @Test
    public void new_keynumbers_can_be_created_and_persisted_adhoc() {
        final String newKey = "YET-NON-EXISTING";
        try {
            coKeyNumber.currNumber(newKey);
            fail("Getting the next number for non existing key should fail.");
        } catch (final EntityCompanionException ex) {
            assertEquals(String.format("No number associated with key [%s].", newKey), ex.getMessage());
        }
        
        final Integer expected = 1;
        assertEquals("Incorrect initial number.", expected, coKeyNumber.nextNumber(newKey));
        assertEquals("Incorrect current number.", expected, coKeyNumber.currNumber(newKey));
    }
    
    @Test
    public void existing_keynumber_value_is_retrievable_by_key() {
        assertEquals("Incorrect current WO number.", Integer.valueOf(500), coKeyNumber.currNumber("WO"));
    }

    @Test
    public void nextNumber_returns_the_next_keynumber_value_and_simultaneously_persists_it() {
        final Integer nextNumber = 501;
        assertEquals("Incorrectly generated next WO number.", nextNumber, coKeyNumber.nextNumber("WO"));
        assertEquals("Incorrect current WO number after generating the next number.", nextNumber, coKeyNumber.currNumber("WO"));
    }

    @Test
    public void nextNumbers_with_count_1_is_equivalent_to_nextNumber() {
        final Integer nextNumber = 501;
        final SortedSet<Integer> numbers = coKeyNumber.nextNumbers("WO", 1);
        assertEquals("Unexpected number of generated values.", 1, numbers.size());
        assertEquals("Incorrectly generated next WO number.", nextNumber, numbers.first());
        assertEquals("Incorrect current WO number after generating the next number.", nextNumber, coKeyNumber.currNumber("WO"));
    }

    @Test
    public void nextNumbers_generated_the_requested_number_of_key_values_as_sorted_set_and_persists_the_last_one() {
        final SortedSet<Integer> expectedNumbers = new TreeSet<Integer>();
        expectedNumbers.add(501);
        expectedNumbers.add(502);
        expectedNumbers.add(503);
        expectedNumbers.add(504);
        expectedNumbers.add(505);
        final SortedSet<Integer> numbers = coKeyNumber.nextNumbers("WO", 5);
        assertEquals("Unexpected number of generated values.", 5, numbers.size());
        assertEquals(expectedNumbers, numbers);
        assertEquals("Incorrect current WO number after generating the next number.", numbers.last(), coKeyNumber.currNumber("WO"));
    }

    @Test
    public void nextNumbers_for_the_count_of_less_than_1_returns_an_empty_set() {
        assertTrue("Empty set is expected.", coKeyNumber.nextNumbers("WO", 0).isEmpty());
        assertTrue("Empty set is expected.", coKeyNumber.nextNumbers("WO", -1).isEmpty());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(KeyNumber.class, "WO").setValue("500"));
    }

}
