package ua.com.fielden.platform.security.session;

import com.google.inject.ImplementedBy;

import ua.com.fielden.platform.error.Result;

import java.util.Objects;
import java.util.Optional;


/// This is a contract for integrating Single Sign-On (SSO) session management into the TG authentication life cycle.
/// The actual implementation would be at the level of specific TG-based applications or specialised libraries.
///
/// Some terms:
/// * OP – OpenID Connect Provider
/// * RP – Relying Party
///
@ImplementedBy(DefaultDoNothingSsoSessionControllerImpl.class)
public interface ISsoSessionController {

    /// This method should issue a request to refresh an access token, for example,
    /// by using the refresh token and account information of an SSO session, identified by `sid`.
    /// Its implementation should take into account app server restarts and concurrent requests,
    /// where an established SSO session was not yet restored from its persistent state.
    ///
    /// Unsuccessful results should indicate situations where OP refused to refresh a token.
    /// Such situations should lead to app session invalidation and removal of the SSO session.
    ///
    /// @param sid  session identifier, which could be OP-specific, such Session ID used by Microsoft Entra ID,
    ///             or an OIDC claim `sub` from ID tokens.
    /// @return  result indicating success of failure.
    ///
    Result refresh(final String sid);

    /// This method should remove an SSO session, identified by {@code sid} from memory and persistent storage.
    ///
    /// @param sid  session identifier
    ///
    void invalidate(final String sid);

    /// Retrieves an ID, refresh, and access tokens, associated with `sid`.
    /// It is expected that empty result should only be returned were no tokens, associated with `sid`, could be found.
    ///
    /// @param sid  session identifier
    /// @return  token (optional).
    ///
    default Optional<Tokens> tokens(final String sid) {
        return Optional.empty();
    }

    /// Record that represents ID, refresh, and access tokens.
    ///
    record Tokens(String idToken, String refreshToken, String accessToken) {
        public Tokens {
            Objects.requireNonNull(idToken, "idToken cannot be null");
            Objects.requireNonNull(refreshToken, "refreshToken cannot be null");
            Objects.requireNonNull(accessToken, "accessToken cannot be null");
        }
    }

}