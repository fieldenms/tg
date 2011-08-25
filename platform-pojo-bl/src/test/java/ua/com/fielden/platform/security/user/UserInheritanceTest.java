package ua.com.fielden.platform.security.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import com.google.inject.Injector;


/**
 * This test case ensures correctness of the basic application user rules pertaining to the user inheritance logic.
 *
 * @author TG Team
 *
 */
public class UserInheritanceTest {

    final Injector injector = new ApplicationInjectorFactory().add(new CommonTestEntityModuleWithPropertyFactory()).getInjector();
    final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private User baseUser;
    private User user;


    @Before
    public void setUp() {
	baseUser = factory.newEntity(User.class, 1L, "BASE", "Some base but not in-built user");
	baseUser.setBase(true);
	user = factory.newEntity(User.class, 2L, "USER", "Ordinary user");
	user.setBasedOnUser(baseUser);
    }

    @Test
    public void test_that_base_user_does_not_have_a_based_on_user_after_creation() {
	assertNull("There should be no based on user for the base user.", baseUser.getBasedOnUser());
    }

    @Test
    public void test_that_only_base_user_can_be_used_for_inheritance() {
	user.setBase(true);
	user.setBasedOnUser(baseUser);
	assertNotNull("Base user should have been accepted.", user.getBasedOnUser());
	assertEquals("Unexpected base user.", baseUser, user.getBasedOnUser());

	assertTrue("basedOnUser for baseUser should be valid at this stage", baseUser.getProperty("basedOnUser").isValid());
	baseUser.setBasedOnUser(user);
	assertFalse("basedOnUser for baseUser should not be valid at this stage", baseUser.getProperty("basedOnUser").isValid());
    }

    @Test
    public void test_that_self_reference_in_not_permitted() {
	user.setBase(true);
	assertTrue("basedOnUser for baseUser should be valid at this stage", user.getProperty("basedOnUser").isValid());
	user.setBasedOnUser(user);
	assertTrue("User should remain base.", user.isBase());
	assertFalse("basedOnUser for baseUser should not be valid at this stage", user.getProperty("basedOnUser").isValid());
    }


    @Test
    public void test_that_when_base_is_set_then_based_on_user_becomes_null() {
	user.setBase(true);
	assertNull("When user becomes base its basedOnUser should become null.", user.getBasedOnUser());
    }

    @Test
    public void test_that_when_based_on_user_is_set_then_base_becomes_false() {
	user.setBase(true);
	baseUser.setBasedOnUser(user);
	assertFalse("baseUser should have become not base once based user was set for it.", baseUser.isBase());
    }

}
