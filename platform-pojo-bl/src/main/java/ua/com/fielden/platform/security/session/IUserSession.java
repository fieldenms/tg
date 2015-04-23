package ua.com.fielden.platform.security.session;

import java.util.Optional;

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
     * Constructs an updated user session for the specified user, but only if the provided <code>authenticator</code> is valid for the
     * specified <code>user</code> and associated with one of the active user sessions.
     * <p>
     * The process of authenticator validation includes hash verification using the specified <code>key</code>, expirations and user association checks.
     * <p>
     * An empty result is returned if the authenticator does not pass validation.
     * If a stale user session was discovered as part of the validation process, it gets deleted.
     * <p>
     * A non-empty result contains an updated user session with a new <code>authenticator</code>.
     *
     * @param authenticator
     * @param key
     * @return
     */
    Optional<UserSession> currentSession(final User user, final String authenticator);

    /**
     * This method creates a new session for the provided users.
     * It is assumed that the user has been explicitly authenticated by the system before making a call to this method.
     * <p>
     * It is considered that a new user session can always be created for the provided user.
     * The resultant instance of the user session contains a new authenticator that should be associated with a response to the user's request.
     * This establishes the server trust to the future user requests.
     * <p>
     * Argument <code>isDeviceTruested</code> indicates whether the session is being established from trusted or untrusted devices.
     * This provides hints to the system to establish a long or short lived sessions.
     * <p>
     * The <code>key</code> is used to produce the authenticator and to encode the dynamically generated series id for the session.
     *
     * @param key
     * @return
     */
    UserSession newSession(final User user, final boolean isDeviceTrusted);

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