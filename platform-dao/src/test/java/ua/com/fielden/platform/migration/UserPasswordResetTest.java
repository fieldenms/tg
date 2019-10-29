package ua.com.fielden.platform.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.error.Result.failure;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserSecret;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * This is a test for user password reset utility.
 * 
 * @author TG Team
 * 
 */
public class UserPasswordResetTest extends AbstractDaoTestCase {
    private final IUser coUser = co$(User.class);
    private final IUserSecret coUserSecret = co$(UserSecret.class);
    private final ResetUserPassword passwordReset = new ResetUserPassword(coUser);

    @Test
    public void test_that_utility_resets_password_for_all_users() throws Exception {
        passwordReset.resetAll();

        final List<User> users = coUser.findAllUsers();
        for (final User user : users) {
            final UserSecret secret = coUserSecret.findByUsername(user.getKey()).orElseThrow(() -> failure("Missing user secret."));
            assertEquals("Incorrect password.", secret.getPassword(),  coUserSecret.hashPasswd(user.getKey(), secret.getSalt()));
        }
    }

    @Test
    public void test_that_utility_resets_password_for_an_individual_user() throws Exception {
        passwordReset.reset("USER1");

        final Optional<UserSecret> maybeSecret = coUserSecret.findByUsername("USER1");
        assertTrue(maybeSecret.isPresent());
        final UserSecret secret = maybeSecret.get();
        assertEquals(secret.getPassword(),  coUserSecret.hashPasswd(secret.getKey().getKey(), secret.getSalt()));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final User user1 = save(new_(User.class, "USER1").setBase(true).setEmail("USER1@unit-test.software").setActive(true));
        coUserSecret.findByUsername(user1.getKey()).ifPresent(s -> save(s.setPassword("PASSWORD-1")));
        
        final User user2 = save(new_(User.class, "USER2").setBase(true).setEmail("USER2@unit-test.software").setActive(true));
        coUserSecret.findByUsername(user2.getKey()).ifPresent(s -> save(s.setPassword("PASSWORD-2")));
    }
}