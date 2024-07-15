package ua.com.fielden.platform.security.session;

import com.google.inject.ImplementedBy;

import ua.com.fielden.platform.error.Result;

/**
 * This is a contract for integrating SSO sessions into the TG authentication life cycle.
 * The actual implementation would be at the level of specific TG-based applications.
 * <p>
 * Some terms:
 * <ul>
 * <li>OP – OpenID Connect Provider
 * <li>RP – Relying Party
 * </ul>
 * 
 * @author TG Team
 */
@ImplementedBy(DefaultDoNothingSsoSessionControllerImpl.class)
public interface ISsoSessionController {

    /**
     * This method should issue a request to refresh an access token, for example, by using the refresh token and account information of an SSO session, identified by {@code sid}.
     * Its implementation should take into account app server restarts, where an established SSO session was not yet restored from its persistent state.
     * <p>
     * Unsuccessful results should indicate situations where OP refused to refresh a token.
     * Such situations should lead to app session invalidation and removal of the SSO session at the app side.
     *
     * @param sid
     * @return
     */
    Result refresh(final String sid);

    /**
     * This method should remove an SSO session, identified by {@code sid} from memory and persistent storage.
     *
     * @param sid
     */
    void invalidate(final String sid);

}