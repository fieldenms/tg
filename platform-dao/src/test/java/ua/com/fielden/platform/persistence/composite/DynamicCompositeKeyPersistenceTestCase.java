package ua.com.fielden.platform.persistence.composite;

import org.hibernate.Session;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.types.Money;

/**
 * Implements test cases for using entity with composite key, which includes testing of basic functionality such as method equals, as well as Hibernate interaction.
 *
 * @author 01es
 *
 */
public class DynamicCompositeKeyPersistenceTestCase extends DbDrivenTestCase {
    /**
     * Tests correctness of methods equals, hashCode and compareTo
     */
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

    /**
     * Tests Hibernate interaction.
     */
    public void testThatEntityIsCorrectlyRetrievedFromDb() {
	final String keyPartOne = "key-1-1";
	final EntityWithMoney keyPartTwo = new EntityWithMoney("key1", "desc", new Money("20.00"));

	final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
	final EntityWithDynamicCompositeKey result = (EntityWithDynamicCompositeKey) session.load(EntityWithDynamicCompositeKey.class, 1L);

	assertEquals("Incorrect part one of the key.", keyPartOne, result.getKeyPartOne());
	assertEquals("Incorrect part tow of the key.", keyPartTwo, result.getKeyPartTwo());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] {"src/test/resources/data-files/entity-with-dynamic-composite-key-test-case.flat.xml"};
    }
}