package ua.com.fielden.platform.persistence.composite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.hibernate.Session;
import org.junit.Test;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

/**
 * Implements test cases for using entity with composite key, which includes testing of basic functionality such as method equals, as well as DB interaction.
 *
 * @author TG Team
 *
 */
public class DynamicCompositeKeyPersistenceTestCase extends AbstractDaoTestCase {

    private static final String keyPartOne = "key-1-1";

    @Test
    public void testThatCommonMethodsAreCorrectlyImplemented() {
        final String keyPartOne = "key-part-one";
        final EntityWithMoney keyPartTwo = new EntityWithMoney("key", "desc", new Money("200.00"));
        final EntityWithDynamicCompositeKey entity = new EntityWithDynamicCompositeKey(keyPartOne, keyPartTwo);

        final DynamicEntityKey key = entity.getKey();
        assertEquals("Incorrectly calculated hash code.", key.hashCode() * 23, entity.hashCode());

        final EntityWithDynamicCompositeKey entity2 = new EntityWithDynamicCompositeKey(keyPartOne, keyPartTwo);
        assertEquals("Should be equal.", entity, entity2);

        final EntityWithDynamicCompositeKey entity3 = new EntityWithDynamicCompositeKey("key-part-two", keyPartTwo); // "key-part-one" < "key-part-two"
        assertTrue("Incorrect comparison result", entity.compareTo(entity3) < 0);
    }

    @Test
    public void key_members_are_corectly_populated_upon_retrieval_of_entities_with_a_composite_key() {
        final EntityWithMoney ewm = co(EntityWithMoney.class).findByKey("key1");
        assertNotNull(ewm);
        final EntityWithDynamicCompositeKey result = co(EntityWithDynamicCompositeKey.class).findByKey(keyPartOne, ewm);
        assertNotNull(result);
        
        assertEquals(keyPartOne, result.getKeyPartOne());
        assertEquals(ewm, result.getKeyPartTwo());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final EntityWithMoney ewm = save(new_(EntityWithMoney.class, "key1", "desc").setMoney(Money.of("20.00")));
        save(new_composite(EntityWithDynamicCompositeKey.class, keyPartOne, ewm));
    }
}