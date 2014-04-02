package ua.com.fielden.platform.security.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * This test case ensures correctness of the basic application user rules pertaining to the behaviour of the system in-built user accounts.
 * 
 * @author TG Team
 * 
 */
public class InBuiltUserTest {

    final Injector injector = new ApplicationInjectorFactory().add(new CommonTestEntityModuleWithPropertyFactory()).getInjector();
    final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private User su;
    private User baseUser;

    @Before
    public void setUp() {
        su = factory.newEntity(User.class, 1L, User.system_users.SU.name(), "Super user");
        baseUser = factory.newEntity(User.class, 2L, "BASE", "Some base but not in-built user");
        baseUser.setBase(true);
    }

    @Test
    public void test_that_in_built_user_is_base() {
        assertTrue("In-built user should be base after creation.", su.isBase());
    }

    @Test
    public void test_in_built_user_cannot_be_renamed() {
        assertTrue("Key should be valid at this stage", su.getProperty("key").isValid());
        su.setKey("NEW VALUE");
        assertFalse("Key should not be valid at this stage", su.getProperty("key").isValid());
    }

    @Test
    public void test_that_key_can_be_set_for_in_built_user() {
        assertTrue("Key should be valid at this stage", su.getProperty("key").isValid());
        su.setKey(User.system_users.SU.name());
        assertTrue("Key should be valid at this stage", su.getProperty("key").isValid());
        assertEquals("Incorrect user name.", User.system_users.SU.name(), su.getKey());
    }

    @Test
    public void test_that_in_built_user_cannot_be_based_on_other_users() {
        assertTrue("The baseOnUser property should be valid at this stage", su.getProperty("basedOnUser").isValid());
        su.setBasedOnUser(baseUser);
        assertFalse("The baseOnUser property should not be valid at this stage", su.getProperty("basedOnUser").isValid());
    }

    @Test
    public void test_that_in_built_user_can_only_be_a_based_user() {
        assertTrue("The base property should be valid at this stage", su.getProperty("base").isValid());
        su.setBase(false);
        assertFalse("The base property should not be valid at this stage", su.getProperty("base").isValid());
    }

    @Test
    public void test_that_in_built_user_can_be_made_a_based_user() {
        assertTrue("The base property should be valid at this stage", su.getProperty("base").isValid());
        su.setBase(true);
        assertTrue("The base property should be valid at this stage", su.getProperty("base").isValid());
        assertTrue("In-built users should be base users.", su.isBase());
    }
}
