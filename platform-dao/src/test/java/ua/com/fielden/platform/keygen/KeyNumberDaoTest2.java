package ua.com.fielden.platform.keygen;

import ua.com.fielden.platform.test.DbDrivenTestCase2;

/**
 * Ensures correct generation of work order numbers.
 *
 * @author 01es
 *
 */
public class KeyNumberDaoTest2 extends DbDrivenTestCase2 {
    private final IKeyNumberGenerator keyNumberGen = injector.getInstance(IKeyNumberGenerator.class);

    public void testThatCurrentWoNoIsRetrievedCorrectly() {
	assertEquals("Incorrect current WONO.", new Integer("500"), keyNumberGen.currNumber("WO"));
    }

    public void testThatNextWoNoIsGenereatedCorrectly() {
	assertEquals("Incorrectly generated next WONO.", new Integer("501"), keyNumberGen.nextNumber("WO"));
	assertEquals("Incorrect current WONO after generation.", new Integer("501"), keyNumberGen.currNumber("WO"));
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/key-number-test-case.flat.xml" };
    }

}
