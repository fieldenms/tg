package ua.com.fielden.platform.migration;

import java.util.List;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.DbDrivenTestCase;

/**
 * This is a test for user password reset utility.
 * 
 * @author TG Team
 * 
 */
public class UserPasswordResetTest extends DbDrivenTestCase {
    private final IUser coUser = injector.getInstance(IUser.class);
    private final ResetUserPassword passwordReset = new ResetUserPassword(coUser);

    public void test_that_utility_resets_password_for_all_users() throws Exception {
        passwordReset.resetAll();

        hibernateUtil.getSessionFactory().getCurrentSession().close();

        final List<User> users = coUser.findAllUsers();
        for (final User user : users) {
            assertEquals("Incorrect password.", user.getPassword(),  coUser.hashPasswd(user.getKey(), user.getSalt()));
        }
    }

    public void test_that_utility_resets_password_for_an_individual_user() throws Exception {
        passwordReset.reset("USER-1");

        hibernateUtil.getSessionFactory().getCurrentSession().close();

        final User user = coUser.findByKey("USER-1");
        assertEquals("Incorrect password.", user.getPassword(),  coUser.hashPasswd(user.getKey(), user.getSalt()));
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
        return new String[] { "src/test/resources/data-files/user-password-reset-test-data.flat.xml" };
    }
}