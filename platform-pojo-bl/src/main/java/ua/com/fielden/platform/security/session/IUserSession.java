package ua.com.fielden.platform.security.session;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.security.user.User;

import java.util.Date;
import java.util.Optional;

/// Companion object for entity [UserSession].
///
public interface IUserSession extends IEntityDao<UserSession>, ISaveWithFetch<UserSession> {

    /// Constructs an updated session for `user`, but only if `authenticator` is valid for `user` and associated with one of the active user sessions.
    ///
    /// The process of authenticator validation includes hash verification, expirations and user association checks.
    ///
    /// An empty result is returned if the authenticator does not pass validation.
    /// If a stale user session was discovered as part of the validation process, it gets deleted.
    /// All user sessions get cleared in case of a suspected authenticator theft.
    ///
    /// A non-empty result contains an updated user session with a new `authenticator`.
    /// However, authenticator/session pairs get cached for a short period of time (e.g. 2 minutes) for performance reasons.
    /// All cached values are considered valid.
    /// Cache hits return the same results without re-generation.
    ///
    /// Authenticator theft scenario: in case of successful recognition, but unsuccessful authenticator location in the database,
    /// all sessions for the same user get invalidated.
    ///
    /// @param shouldConsiderTheftScenario  if `true`, authenticator theft scenario is on
    /// @param skipRegeneration  if `true`, a new authenticator will NOT be generated to replace the current one;
    ///                          if `false`, regeneration may take place, depending on whether values are cached
    ///
    Optional<UserSession> currentSession(User user, String authenticator, boolean shouldConsiderTheftScenario, boolean skipRegeneration);

    /// The same as [IUserSession#currentSession(User, String, boolean, boolean)] with argument `shouldConsiderTheftScenario` set to `true`.
    ///
    /// @param skipRegeneration  if `true`, a new authenticator will NOT be generated to replace the current one;
    ///                          if `false`, regeneration may take place, depending on whether values are cached
    ///
    Optional<UserSession> currentSession(User user, String authenticator, boolean skipRegeneration);

    /// This method creates and persists a new session for `user`.
    /// It is assumed that the user was explicitly authenticated by the system before this call, and that a new user session
    /// can always be created for the user.
    ///
    /// The returned user session contains a new authenticator that should be associated with a response to the user's request.
    /// This establishes server trust for future user requests.
    ///
    /// @param isDeviceTrusted  indicates whether the session is being established from trusted or untrusted devices.
    ///                         This provides hints to the system when establishing long- or short-lived sessions.
    /// @param sid  a session id for SSO, if any; pass `null` if none is available or required
    ///
    UserSession newSession(User user, boolean isDeviceTrusted, @Nullable String sid);

    /// Generates cryptographically strong series that is used as session id.
    ///
    String genSeriesId();

    /// Creates a session authenticator.
    ///
    Authenticator mkAuthenticator(User user, String seriesId, long version,  Date expiryTime);

    /// Invalidates the specified user session.
    ///
    void clearSession(UserSession session);
    
    /// Delete all sessions associated with `user`.
    /// Having no current sessions, enforces the user to login explicitly.
    /// This method should be used only when a security threat is suspected or a user account needs to be disabled.
    ///
    /// @return the number of removed sessions
    ///
    int clearAll(User user);

    /// Clears all sessions that are associated with untrusted devices for `user`.
    ///
    void clearUntrusted(User user);

    /// Clears all expired sessions for `user`.
    ///
    void clearExpired(User user);

    /// Removes all sessions for all users.
    ///
    void clearAll();

    /// Clears all sessions that are associated with untrusted devices for all users.
    ///
    void clearUntrusted();

    /// Clears all expired sessions for all users.
    ///
    void clearExpired();
    
    /// Clears all sessions that match `sid`, both persisted and cached.
    ///
    /// Cached sessions are cleared even if clearing persisted sessions ends with an exception, which then propagates.
    /// A session is valid as long as either its persisted or cached copy exists.
    ///
    /// @return the number of deleted persisted sessions matching `sid`
    ///
    int clearAllWithSid(String sid);

}
