package ua.com.fielden.platform.dao.username;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.DefaultFilter;
import ua.com.fielden.platform.equery.interfaces.IFilter;

/**
 * A test case to cover username assignment in {@link CommonEntityDao}
 *
 * @author TG Team
 *
 */
public class UsernameAssignmentTest {

    private static final String USER_NAME = "user";

    private final IFilter filter = new DefaultFilter();

    @Test
    public void test_username_is_assigned() {
	final TopLevelDao dao = new TopLevelDao(null, filter);
	assertNull("Initial username value should be null.", dao.getUsername());
	dao.setUsername(USER_NAME);
	assertEquals("Unexpected values for username.", USER_NAME, dao.getUsername());
    }

    @Test
    public void test_username_is_prolifirated_to_all_aggregated_dao() {
	final TopLevelDao dao = new TopLevelDao(new EmbeddedDao(filter), filter);
	assertNull("Initial username value for aggregated DAO should be null.", dao.getDao().getUsername());
	dao.setUsername(USER_NAME);
	assertEquals("Unexpected values for username of aggregated DAO.", USER_NAME, dao.getDao().getUsername());
    }

    @Test
    public void test_that_setting_of_username_handles_cyclic_references() {
	final TopLevelDao dao = new TopLevelDao(new EmbeddedDao(filter), filter);
	dao.getDao().setDao(dao); // make cyclic reference
	dao.setUsername(USER_NAME);
	assertEquals("Unexpected values for username.", USER_NAME, dao.getUsername());
	assertEquals("Unexpected values for username.", USER_NAME, dao.getDao().getUsername());
	assertEquals("Unexpected values for username.", USER_NAME, dao.getDao().getDao().getUsername());
    }

    @Test
    public void test_username_null_cannot_be_assigned() {
	final TopLevelDao dao = new TopLevelDao(null, filter);
	try {
	    dao.setUsername(null);
	    fail("Setting null username should have raised an exception");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_that_setting_of_a_different_username_is_not_permitted() {
	final TopLevelDao dao = new TopLevelDao(null, filter);
	dao.setUsername(USER_NAME);
	try {
	    dao.setUsername("another username");
	    fail("Setting another username should have raised an exception");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_that_setting_of_the_same_username_is_handled_gracefully() {
	final TopLevelDao dao = new TopLevelDao(null, filter);
	dao.setUsername(USER_NAME);
	dao.setUsername(USER_NAME);
	assertEquals("Unexpected values for username.", USER_NAME, dao.getUsername());
    }

}
