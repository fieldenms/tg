package ua.com.fielden.platform.migration;

import java.util.List;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;

/**
 * This is a utility, which should be used to reset passwords for all users. The need to do that is driven by migration from the old password encryption schema to a new one based
 * on the RSA algorithm.
 * <p>
 * It is envisaged that Fleet Pilot application will provided a facility for individual user to specify their own password after the initial login using a reset password.
 * <p>
 * The reset password equals to user's login name and stored in the database in the encoded with application wide private key form.
 * 
 * @author TG Team
 * 
 */
public class ResetUserPassword {
    private final IUser coUser;

    public ResetUserPassword(final IUser coUser) {
        this.coUser = coUser;
    }

    /**
     * Resets password for all users.
     * 
     * @throws Exception
     */
    public void resetAll() {
        final List<User> users = coUser.findAllUsers();
        for (final User user : users) {
            coUser.resetPasswd(user, user.getKey());
        }
    }

    /**
     * Resets password for one specified user.
     * 
     * @param username
     * @param privateKey
     * @throws Exception
     */
    public void reset(final String username) {
        final User user = coUser.findByKey(username);
        coUser.resetPasswd(user, user.getKey());
    }
}
