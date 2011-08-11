/**
 *
 */
package ua.com.fielden.platform.test.query;

import ua.com.fielden.platform.basic.autocompleter.EntityQueryValueMatcher;
import ua.com.fielden.platform.test.DbDrivenTestCase;

/**
 * Test for {@link EntityQueryValueMatcher}
 *
 * @author Yura, Oleh
 */
public class EntityQueryValueMatcherTestCase extends DbDrivenTestCase {

    public void testFindMatches() {
	// FIXME need to correct the test
	//final IMixer<Rotable> rotableQuery = rotableDao.query().property("location.id").eq(101l);//.property("location.key").eq("WS1");
	//final EntityQueryValueMatcher<Rotable> rotableMatcher = new EntityQueryValueMatcher<Rotable>(rotableQuery, "key");
	// TODO this test fails because result of rotables is returned in the form of 2 queries (one for bogies and one for wheelset) - and only the last query is taken into account
//	assertEquals(6, rotableMatcher.findMatches("BO%").size());
//	assertEquals(7, rotableMatcher.findMatches("WS%").size());
//	assertEquals(0, rotableMatcher.findMatches("WSET04").size());
//	assertEquals(0, rotableMatcher.findMatches("BOGIE03").size());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }

}
