package ua.com.fielden.platform.security.session;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.security.SignatureException;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.security.session.Authenticator.mkToken;

/// DAO implementation for companion object {@link IUserSession}.
///
@EntityType(UserSession.class)
public class UserSessionDao extends CommonEntityDao<UserSession> implements IUserSession {
    private final Logger logger = getLogger(UserSessionDao.class);
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /// A key to be used for hashing authenticators and series ID before storing them.
    private final String hashingKey;
    private final int trustedDurationMins;
    private final int untrustedDurationMins;
    private final SessionIdentifierGenerator crypto;
    private final Cache<String, UserSession> cache;
    private final IUniversalConstants constants;
    private final ISsoSessionController ssoSessionController;

    @Inject
    public UserSessionDao(
            final @SessionHashingKey String hashingKey,
            final @TrustedDeviceSessionDuration int trustedDurationMins,
            final @UntrustedDeviceSessionDuration int untrustedDurationMins,
            final @SessionCache Cache<String, UserSession> cache,
            final IUniversalConstants constants,
            final SessionIdentifierGenerator crypto,
            final ISsoSessionController ssoSessionController)
    {
        this.constants = constants;
        this.hashingKey = hashingKey;
        this.trustedDurationMins = trustedDurationMins;
        this.untrustedDurationMins = untrustedDurationMins;
        this.crypto = crypto;
        this.cache = cache;
        this.ssoSessionController = ssoSessionController;
    }

    @Override
    @SessionRequired
    public Either<Long, UserSession> save(final UserSession entity, final Optional<fetch<UserSession>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

    @Override
    @SessionRequired
    public void clearSession(final UserSession session) {
        cache.invalidate(findAuthenticator(session));
        defaultDelete(session);
    }
    
    @Override
    @SessionRequired
    public int clearAll(final User user) {
        final EntityResultQueryModel<UserSession> q = select(UserSession.class).where().prop("user").eq().val(user).model();

        final int count = count(q);
        if (count > 0) {
            logger.info(() -> "Removing [%s] sessions for user [%s].".formatted(count, user.getKey()));
            
            invalidateCache(q);
            
            defaultBatchDelete(q);
        }
        return count;
    }

    @Override
    @SessionRequired
    public void clearUntrusted(final User user) {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                        .where()
                        .prop("user").eq().val(user)
                        .and().prop("trusted").eq().val(false)
                        .model();
        
        invalidateCache(query);

        defaultBatchDelete(query);
    }

    @Override
    @SessionRequired
    public void clearAll() {
        defaultBatchDelete(select(UserSession.class).model());
        cache.invalidateAll();
    }

    @Override
    @SessionRequired
    public void clearUntrusted() {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                        .where()
                        .prop("trusted").eq().val(false)
                        .model();

        invalidateCache(query);
        
        defaultBatchDelete(query);
    }

    @Override
    @SessionRequired
    public void clearExpired(final User user) {
        try {
            final EntityResultQueryModel<UserSession> query =
                    select(UserSession.class)
                            .where()
                            .prop("user").eq().val(user)
                            .and().prop("expiryTime").lt().now()
                            .model();
            
            invalidateCache(query);
            
            defaultBatchDelete(query);
        } catch (final Exception ex) {
            logger.error(() -> "Could not clear expired sessions for user [%s].".formatted(user), ex);
        }
    }

    @Override
    @SessionRequired
    public void clearExpired() {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                        .where()
                        .prop("expiryTime").lt().now()
                        .model();
        
        invalidateCache(query);
        
        defaultBatchDelete(query);
    }

    @Override
    public int clearAllWithSid(final String sid) {
        // If `sid` is empty, no session can be identified for clearing/invalidation.
        if (StringUtils.isEmpty(sid)) {
            return 0;
        }

        // Delete user sessions and SSO sessions from the database in a separate transaction.
        final int count = deleteSessionsBySid(sid);
        logger.info(() -> "User sessions deleted [%s] for sid [%s].".formatted(count, sid));

        // Delete all matching user sessions from cache.
        final List<String> keys = cache.asMap().entrySet().stream().filter(p -> sid.equals(p.getValue().getSid())).map(Map.Entry::getKey).collect(toList());
        cache.invalidateAll(keys);

        return count;
    }

    /// This method is used strictly to enforce deletion in a separate transaction.
    ///
    /// @param sid  SSO session identifier.
    /// @return  the number of deleted sessions.
    @SessionRequired(allowNestedScope = false)
    protected int deleteSessionsBySid(final String sid) {
        // First, try invalidating SSO sessions, but do not fail in case exceptions as this is a less critical part.
        try {
            ssoSessionController.invalidate(sid);
        } catch (final Exception ex) {
            logger.error(() -> "Could not invalidate SSO sessions for sid [%s].".formatted(sid), ex);
        }

        // Second, but more importantly, invalidate user sessions.
        try {
            return this.defaultBatchDelete(select(UserSession.class).where().prop("sid").eq().val(sid).model());
        } catch (final Exception ex) {
            logger.error(() -> "Could not delete user sessions by sid [%s].".formatted(sid), ex);
            return 0;
        }
    }

    /// A helper method to remove sessions from cache.
    ///
    /// @param q  a query for retrieving user session to be removed from cache.
    ///
    private void invalidateCache(final EntityResultQueryModel<UserSession> q) {
        stream(from(q).with(fetchAll(UserSession.class)).model())
        .forEach(session -> cache.invalidate(findAuthenticator(session)));
    }

    /// Removes all sessions that are associated with users that have been associated with the provided series ID. There should at most be only one such user (but potentially
    /// multiple sessions) due to cryptographic randomness of series IDs.
    ///
    /// @param seriesId  a session identifier.
    /// @return  the number of removed sessions.
    ///
    @SessionRequired
    protected int removeSessionsForUsersBy(final String seriesId) {
        final var query = select(UserSession.class).where().prop("seriesId").eq().val(seriesHash(seriesId)).model();

        // Count the number of sessions to be removed...
        final int count = count(query);
        if (count > 0) {
            // Get all those sessions and using their users, clear all sessions (there could more than the ones retrieved) associated with those users
            logger.warn(() -> "Removing specific session (%s) for all affected users due to a suspected stolen session authenticator.".formatted(count));
            final var qem = from(query).with(fetchAll(UserSession.class)).model();
            stream(qem).forEach(ss -> clearAll(ss.getUser()));
        }

        return count;
    }

    /// A convenient method for hashing the passed in series id.
    ///
    /// @param seriesId  a session identifier.
    /// @return  a cryptographically secure hash for `sessionId`.
    ///
    private String seriesHash(final String seriesId) {
        final String seriesIdHash;
        try {
            seriesIdHash = crypto.calculateRFC2104HMAC(seriesId, hashingKey);
        } catch (final SignatureException ex) {
            final String msg = "Could not hash a series ID.";
            logger.error(msg, ex);
            throw new SecurityException(msg, ex);
        }
        return seriesIdHash;
    }

    /// The main goal of this method is to validate and refresh the user session, which happens for each request.
    /// The validation part has a very strong role for detecting fraudulent attempts to access the system,
    /// or indications of compromised users with their authenticators stolen.
    ///
    /// If the presented authenticator is present in the cache, then it is considered to be valid.
    ///
    /// Please note that the fact that the first argument is a [User] instance.
    /// This means that the username has already been verified and identified as belonging to an active user account.
    ///
    @Override
    //@SessionRequired -- db session should not be used here
    public Optional<UserSession> currentSession(final User user, final String authenticator, final boolean shouldConsiderTheftScenario, final boolean skipRegeneration) {
        // Reconstruct authenticator from string and then proceed with its validation.
        // In case of validation failure, no reason should be provided to the outside as this could reveal too much information to a potential adversary.
        final Authenticator auth = fromString(authenticator);

        // Check the cache first -- all authenicators in cache are considered valid.
        // But just in case, make sure that the current user matches the authenticator's username.
        final UserSession cachedSession = cache.getIfPresent(authenticator);
        if (cachedSession != null && user.getKey().equals(auth.username)) {
            return Optional.of(cachedSession);
        }

        // The authenticator is not in cache, a full authentication and series id regeneration process is in order.
        // Verify authenticator's authenticity using its hash and the application hashing key.
        try {
            if (!auth.hash.equals(crypto.calculateRFC2104HMAC(auth.token, hashingKey))) {
                // authenticator has been tempered with
                logger.warn(() -> "The provided authenticator %s cannot be verified. A tempered authenticator is suspected.".formatted(auth));
                // Remove user sessions that are identified with the provided series ID.
                // Series ID is the only possibly reliable piece of the authenticator at this stage as it is hard to forge algorithmically.
                // Therefore, in the worst case scenario that an authenticator has been stolen and the series ID is being reused,
                // all sessions for users that are associated with that series ID should be removed.
                // This is because those users (should really be just one due to serial ID cryptographic randomness) have most likely been compromised.
                // It is also possible that the provided series ID is fraudulent and does not match any existing sessions
                // Then, no problems -- just deny the access and request explicit re-authentication.
                final int count = removeSessionsForUsersBy(auth.seriesId);
                logger.debug(() -> "Removed %s session(s) for series ID %s".formatted(count, auth.seriesId));
                return Optional.empty();
            }

        } catch (final SignatureException ex) {
            final String msg = "Could not calculate a hash for session authenticator.";
            logger.error(msg, ex);
            throw new SecurityException(msg, ex);
        }

        // The provided authenticator has been verified based on its content and hash.
        // This, however, does not guarantee that it has not been stolen and is not being used for illegal access to the system.
        // The next logical thing to check is whether the user making the request and the user specified in the authenticator match.
        if (!user.getKey().equals(auth.username)) {
            // Authenticator has been stolen.
            logger.warn(() -> "The provided authenticator %s does not reference the current user %s. A stolen authenticator is suspected.".formatted(auth, user.getKey()));
            // Similarly, as described previously, we need to remove all sessions for user(s) that are associated with the series ID in the presented authenticator.
            final int count = removeSessionsForUsersBy(auth.seriesId);
            logger.debug(() -> "Removed %s session(s) for series ID %s".formatted(count, auth.seriesId));
            return Optional.empty();
        }

        // So far so good and there is a hope that the current request is authentic.
        // However, there is still a chance that it is coming for an adversary...
        // Let's find a persisted session, and there should be one for an authentic request, associated with the specified user and series ID.
        final UserSession session = assignAuthenticator(findByKeyAndFetch(fetchAll(UserSession.class), user, seriesHash(auth.seriesId)), auth.seriesId);

        // If the persisted session does not exist for a seemingly valid authenticator then it is most likely due to an authenticator theft, and here is why:
        // An authenticator could have been stolen, and already successfully used by an adversary to access the system from a different device than the one authenticator was stolen from.
        // When a legitimate user is trying to access the system by presenting the stolen authenticator, which was already used by an adversary (this leads to series ID regeneration),
        // then there would be no session associated with it!!!
        // This means all sessions for this particular user should be invalidated (removed) to stop an adversary from accessing the system.
        if (session == null) {
            // Before failing authentication need to check once again into the cache.
            // This is to prevent false negatives from occurring during concurrent requests from the same user (e.g. loading the application in multiple tabs).
            try {
                Thread.sleep(200);
            } catch (final InterruptedException ex) {
                logger.debug(() -> "Sleep interrupted.");
            }
            final UserSession us = cache.getIfPresent(authenticator);
            if (us != null) {
                return Optional.of(us);
            }

            // Remove all user sessions if the theft scenario should be considered.
            if (shouldConsiderTheftScenario) {
                // If the session was not found in the cache, then proceed with the theft story...
                logger.warn(() -> "A seemingly correct authenticator [%s] did not have a corresponding session record.".formatted(auth));
                // In this case, sessions are removed based on the username and series ID, which are required, taking into consideration that the series ID could have been already regenerated.
                final int count = clearAllFoUserAndBySeriesId(user, auth);
                logger.debug(() -> "Removed %s session(s) for series ID [%s].".formatted(count, auth.seriesId));
                return Optional.empty();
            }
            // Otherwise, just return an empty result, indicating no user session could be found.
            else {
                logger.warn(() -> "A seemingly correct authenticator [%s] did not have a corresponding sesssion record, access denied (skip regeneration == %s).".formatted(auth, skipRegeneration));
                return Optional.empty();
            }
        }

        // Only after we have a high probability for a legitimate user request, the identified session needs to be checked for expiration.
        // The time from the retrieved session should be used.
        // In the case of RSO, expired sessions should be invalidated, and the request denied.
        // In the case of SSO (SID is present), expired sessions can still be re-validated by attempting to refresh the SSO session via the silent flow.
        final boolean sessionExpired = session.getExpiryTime().before(now().toDate());
        final boolean sessionRso = session.getSid() == null;
        if (sessionExpired && sessionRso) {
            logger.warn(() -> "Session for user [%s] has expired at [%s], access denied (skip regeneration == %s).".formatted(user, formatter.print(session.getExpiryTime().getTime()), skipRegeneration));
            // If the authenticator has expired, then use this opportunity to clear all expired sessions for the current user.
            clearExpired(user);
            ssoSessionController.invalidate(session.getSid());
            return empty();
        }

        // If this point is reached, then the identified session is considered valid (almost, as it may still be an expired SSO session).
        // Need to decide whether a new session should be generated.
        // If not, then return the identified session, even if it has expired.
        // In practice, this is limited to SSE requests that never return a response, and thus would not be able to return a new security token back to the client.
        if (skipRegeneration) {
            return of(session);
        }

        // Otherwise, let's generate a new session.
        try {
            // There is a slim chance that there could be a clash of `seriesId` for the same user...
            // In this case, we may need to implement a re-try...
            // But let's first see if that is a problem by logging warning to this effect.
            final UserSession newSession = newSessionToReplaceOld(user, session.isTrusted(), of(authenticator), session.getSid());
            try {
                forceUpdateExpiryTimeForSession(session.getId(), user, now().plusMinutes(untrustedDurationMins));
            } catch (final Exception ex) {
                logger.info(() -> "Old session for user [%s] was not prolonged due to [%s].".formatted(user, ex.getMessage()));
            }

            // Refresh the SSO session for `sid`, which uses a silent flow.
            // In the case of RSO, this would be no op.
            final Result ssoRefreshed = ssoSessionController.refresh(session.getSid());
            if (!ssoRefreshed.isSuccessful()) {
                clearAllWithSid(session.getSid());
                return empty();
            }

            return of(newSession);
        } catch (final Exception ex) {
            logger.warn(() -> "Saving of a new session for user [%s] did not succeed. Using previously verified session, if not expired: [%s].".formatted(user, !sessionExpired), ex);
            return sessionExpired ? empty() : of(session);
        }
    }

    /// Forcibly updates the expiry time for a session that is now replaced with a new one.
    /// Here "forcibly" means without regard for any concurrent modification, ignoring versioning to avoid conflict detection.
    ///
    @SessionRequired(allowNestedScope = false)
    protected void forceUpdateExpiryTimeForSession(final Long oldSessionId, final User user, final DateTime newExpiryTime) {
        getSession().doWork(conn -> {
            try(final PreparedStatement ps = conn.prepareStatement("UPDATE USERSESSION_ SET EXPIRYTIME_ = ? WHERE _ID = ?")) {
                ps.setTimestamp(1, new java.sql.Timestamp(newExpiryTime.getMillis()));
                ps.setLong(2, oldSessionId);
                ps.executeUpdate();
            } catch (final Exception ex) {
                logger.warn(() -> "Could not update expiry time for old session for user [%s].".formatted(user), ex);
            }
        });
    }

    @SessionRequired(allowNestedScope = false)
    protected int clearAllFoUserAndBySeriesId(User user, Authenticator auth) {
        return clearAll(user) + removeSessionsForUsersBy(auth.seriesId);
    }

    @Override
    public final Optional<UserSession> currentSession(final User user, final String authenticator, final boolean skipRegeneration) {
        return currentSession(user, authenticator, true, skipRegeneration);
    }

    @Override
    public String genSeriesId() {
        return crypto.nextSessionId();
    }

    @Override
    @SessionRequired
    public UserSession newSession(final User user, final boolean isDeviceTrusted, final String sid) {
        return newSessionToReplaceOld(user, isDeviceTrusted, empty(), sid);
    }

    /// Creates a new session, puts it into cache, and replaces a session associated with `oldAuthenticator`, if it was provided.
    ///
    @SessionRequired
    protected UserSession newSessionToReplaceOld(final User user, final boolean isDeviceTrusted, final Optional<String> oldAuthenticator, final String sid) {
        // Let's first construct the next series id.
        final String seriesId = genSeriesId();
        final UserSession session = new_().setUser(user).setSeriesId(seriesHash(seriesId)).setSid(sid);
        
        session.setTrusted(isDeviceTrusted);
        final Date expiryTime = calcExpiryTime(isDeviceTrusted);
        session.setExpiryTime(expiryTime);
        session.setLastAccess(constants.now().toDate());

        // Authenticator needs to be computed and assigned after the session has been persisted.
        // Assign authenticator in the way as not to disturb the entity meta-state.
        final UserSession saved = assignAuthenticator(save(session), seriesId);

        // Need to cache the established session as a plain object.
        final UserSession userSessionForCache = saved.copyTo(newPlainEntity(UserSession.class, saved.getId()));
        userSessionForCache.setAuthenticator(saved.getAuthenticator().get());
        oldAuthenticator.ifPresent(auth -> cache.put(auth, userSessionForCache));
        cache.put(saved.getAuthenticator().get().toString(), userSessionForCache);
        return saved;
    }

    /// A convenient method for generating and assigning an authenticator to `userSession`.
    ///
    private UserSession assignAuthenticator(final UserSession userSession, final String seriesId) {
        // Let's be a bit more defensive.
        if (userSession == null) {
            return null;
        }
        try {
            userSession.beginInitialising();
            userSession.setAuthenticator(mkAuthenticator(userSession.getUser(), seriesId /* un-hashed */, userSession.getVersion(), userSession.getExpiryTime()));
        } finally {
            userSession.endInitialising();
        }
        return userSession;
    }

    /// A convenient method for instantiating an authenticator.
    ///
    @Override
    public Authenticator mkAuthenticator(final User user, final String seriesId, final long version, final Date expiryTime) {
        try {
            final String token = mkToken(user.getKey(), seriesId, version);
            final String hash = crypto.calculateRFC2104HMAC(token, hashingKey);
            return new Authenticator(Optional.of(expiryTime), token, hash);
        } catch (final SignatureException ex) {
            final String msg = "Could not make an authenticator.";
            logger.error(msg, ex);
            throw new SecurityException(msg, ex);
        }
    }

    /// A convenient method to find an authenticator key in cache by user session.
    ///
    private String findAuthenticator(final UserSession session) {
        for (final Entry<String, UserSession> value : cache.asMap().entrySet()) {
            if (value.getValue().equals(session)) {
                return value.getKey();
            }
        }
        return "";
    }

    /// Calculates a session expiry time based on the notion of trusted and untrusted devices.
    ///
    private Date calcExpiryTime(final boolean isDeviceTrusted) {
        return (isDeviceTrusted ? constants.now().plusMinutes(trustedDurationMins) : constants.now().plusMinutes(untrustedDurationMins)).toDate();
    }

    public Cache<String, UserSession> getCache() {
        return cache;
    }

}
