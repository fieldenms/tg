package ua.com.fielden.platform.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * This test case ensures correctness of the basic application user rules pertaining to the behaviour of the system in-built user accounts.
 * 
 * @author TG Team
 * 
 */
public class InBuiltUserTest extends AbstractDaoTestCase {

    final EntityFactory factory = getInstance(EntityFactory.class);

    private User su;
    private User baseUser;

    @Before
    public void setUp() {
        su = factory.newEntity(User.class, 1L, User.system_users.SU.name(), "Super user");
        baseUser = factory.newEntity(User.class, 2L, "BASE", "Some base but not in-built user");
        baseUser.setBase(true);
    }

    @Test
    public void SU_is_base() {
        assertTrue("In-built user should be base after creation.", su.isBase());
    }

    @Test
    public void SU_cannot_be_renamed() {
        assertTrue("Key should be valid at this stage", su.getProperty("key").isValid());
        su.setKey("SU1");
        assertFalse("Key should not be valid at this stage", su.getProperty("key").isValid());
    }

    @Test
    public void SU_cannot_be_based_on_other_users() {
        assertTrue("The baseOnUser property should be valid at this stage", su.getProperty("basedOnUser").isValid());
        su.setBasedOnUser(baseUser);
        assertFalse("The baseOnUser property should not be valid at this stage", su.getProperty("basedOnUser").isValid());
    }

    @Test
    public void SU_can_only_be_a_based_user() {
        assertTrue("The base property should be valid at this stage", su.getProperty("base").isValid());
        su.setBase(false);
        assertFalse("The base property should not be valid at this stage", su.getProperty("base").isValid());
    }

}