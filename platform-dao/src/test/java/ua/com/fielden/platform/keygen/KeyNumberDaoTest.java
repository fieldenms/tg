package ua.com.fielden.platform.keygen;

import org.junit.Test;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.test.DbDrivenTestCase;

/**
 * Ensures correct generation of named numbers that are used for automatic entity key generations such as 
 * in case of work orders and purchase orders.
 * 
 * @author TG Team
 * 
 */
public class KeyNumberDaoTest extends DbDrivenTestCase {
    private final IKeyNumber coKeyNumber = injector.getInstance(IKeyNumber.class);

    @Test
    public void test_new_keynumber_can_be_created_and_persisted_adhoc() {
        hibernateUtil.getSessionFactory().getCurrentSession().close();
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
    public void test_existing_keynumber_is_retrievable_by_key() {
        hibernateUtil.getSessionFactory().getCurrentSession().close();
        assertEquals("Incorrect current WO number.", new Integer("500"), coKeyNumber.currNumber("WO"));
    }

    @Test
    public void test_the_next_keynumber_gets_generated_and_simultaneously_persisted() {
        hibernateUtil.getSessionFactory().getCurrentSession().close();
        final Integer nextNumber = 501;
        assertEquals("Incorrectly generated next WO number.", nextNumber, coKeyNumber.nextNumber("WO"));
        assertEquals("Incorrect current WO number after generating the next number.", nextNumber, coKeyNumber.currNumber("WO"));
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
        return new String[] { "src/test/resources/data-files/key-number-test-case.flat.xml" };
    }

}
