package ua.com.fielden.platform.security.session;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.security.user.User;

/**
 * Companion object for entity {@link UserSession}.
 *
 * @author Developers
 *
 */
public interface IUserSession extends IEntityDao<UserSession> {

    /**
     * Delete all sessions associated with the specified uses.
     * Having no current sessions, enforces user to login explicitly.
     * Therefore, this method should really be used by the system only in the event when a security threat is suspected or user account needs to be disabled.
     *
     * @param user
     */
    void clearAll(final User user);

    /**
     * Clears all sessions that are associated with untrusted devices for a given user.
     *
     * @param user
     */
    void clearUntrusted(final User user);

    /**
     * Clears all expired sessions for a given user. Could be used to tiding things up.
     *
     * @param user
     */
    void clearExpired(final User user);

    /**
     * Removes all sessions for all users.
     */
    void clearAll();

    /**
     * Clears all sessions that are associated with untrusted devices for all users.
     */
    void clearUntrusted();

    /**
     * Clears all expired sessions for all users. Could be used to tiding things up.
     *
     * @param user
     */
    void clearExpired();

}