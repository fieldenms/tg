package ua.com.fielden.platform.keygen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
public class KeyNumberDaoTest extends AbstractDaoTestCase {
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
        assertEquals("Incorrect current WO number.", new Integer("500"), coKeyNumber.currNumber("WO"));
    }

    @Test
    public void nextNumber_returns_the_next_keynumber_value_and_simultaneously_persists_it() {
        final Integer nextNumber = 501;
        assertEquals("Incorrectly generated next WO number.", nextNumber, coKeyNumber.nextNumber("WO"));
        assertEquals("Incorrect current WO number after generating the next number.", nextNumber, coKeyNumber.currNumber("WO"));
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(KeyNumber.class, "WO").setValue("500"));
    }

}
