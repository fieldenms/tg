package ua.com.fielden.platform.migration;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * This is a test for user password reset utility.
 * 
 * @author TG Team
 * 
 */
public class UserPasswordResetTest extends AbstractDaoTestCase {
    private final IUser coUser = co(User.class);
    private final ResetUserPassword passwordReset = new ResetUserPassword(coUser);

    @Test
    public void test_that_utility_resets_password_for_all_users() throws Exception {
        passwordReset.resetAll();

        final List<User> users = coUser.findAllUsers();
        for (final User user : users) {
            assertEquals("Incorrect password.", user.getPassword(),  coUser.hashPasswd(user.getKey(), user.getSalt()));
        }
    }

    @Test
    public void test_that_utility_resets_password_for_an_individual_user() throws Exception {
        passwordReset.reset("USER1");

        final User user = coUser.findByKey("USER1");
        assertEquals("Incorrect password.", user.getPassword(),  coUser.hashPasswd(user.getKey(), user.getSalt()));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(User.class, "USER1").setPassword("PASSWORD-1").setEmail("USER1@unit-test.software").setActive(true));
        save(new_(User.class, "USER2").setPassword("PASSWORD-2").setEmail("USER2@unit-test.software").setActive(true));
    }
}