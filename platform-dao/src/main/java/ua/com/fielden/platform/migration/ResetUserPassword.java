package ua.com.fielden.platform.migration;

import java.util.List;

import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.security.provider.IUserEx;
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
    private final IUserEx controller;

    public ResetUserPassword(final IUserEx controller) {
        this.controller = controller;
    }

    /**
     * Resets password for all users. The new passwords are encoded with the provided private key before being stored.
     * 
     * @param privateKey
     * @throws Exception
     */
    public void resetAll(final String privateKey) throws Exception {
        final List<User> users = controller.findAllUsers();
        final Cypher cypher = new Cypher();
        for (final User user : users) {
            final String newPassword = cypher.encrypt(user.getKey(), privateKey);
            user.setPassword(newPassword);
            controller.save(user);
        }
    }

    /**
     * Resets password for one specified user.
     * 
     * @param username
     * @param privateKey
     * @throws Exception
     */
    public void reset(final String username, final String privateKey) throws Exception {
        final User user = controller.findByKey(username);
        final Cypher cypher = new Cypher();
        user.setPassword(cypher.encrypt(user.getKey(), privateKey));
        controller.save(user);
    }
}
