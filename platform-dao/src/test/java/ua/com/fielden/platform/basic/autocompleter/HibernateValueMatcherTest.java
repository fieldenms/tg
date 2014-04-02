package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.DbDrivenTestCase;

public class HibernateValueMatcherTest extends DbDrivenTestCase {

    public void testThatValuesAreMatchedWhenUsingCostructorWithHqlQuery() {
        final HibernateValueMatcher<EntityWithMoney> matcher = new HibernateValueMatcher<EntityWithMoney>("from " + EntityWithMoney.class.getName() + " where key like :in_value", "in_value", hibernateUtil.getSessionFactory());

        assertEquals("Incorrect number of matched values.", 2, matcher.findMatches("a%").size());
        assertEquals("Incorrect number of matched values.", 1, matcher.findMatches("b%").size());
        assertEquals("Incorrect number of matched values.", 0, matcher.findMatches("c%").size());
    }

    public void testThatValuesAreMatchedWhenUsingCostructorWithTypeInfo() {
        final HibernateValueMatcher<EntityWithMoney> matcher = new HibernateValueMatcher<EntityWithMoney>(EntityWithMoney.class, "key", hibernateUtil.getSessionFactory());

        assertEquals("Incorrect number of matched values.", 2, matcher.findMatches("a%").size());
        assertEquals("Incorrect number of matched values.", 1, matcher.findMatches("b%").size());
        assertEquals("Incorrect number of matched values.", 0, matcher.findMatches("c%").size());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
        return new String[] { "src/test/resources/data-files/money-user-type-test-case.flat.xml" };
    }
}
