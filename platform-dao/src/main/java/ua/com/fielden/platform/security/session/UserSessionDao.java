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
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.security.SignatureException;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.security.session.Authenticator.mkToken;

/**
 * DAO implementation for companion object {@link IUserSession}.
 *
 * @author TG Team
 *
 */
@EntityType(UserSession.class)
public class UserSessionDao extends CommonEntityDao<UserSession> implements IUserSession {
    private final Logger logger = getLogger(UserSessionDao.class);
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /** A key to be used for hashing authenticators and series ID before storing them. */
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
            final ISsoSessionController ssoSessionController,
            final IFilter filter) {
        super(filter);
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
            logger.info(format("Removing [%s] sessions for user [%s].", count, user.getKey()));
            
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
            logger.error(format("Could not clear expired sessions for user [%s].", user), ex);
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
        // if sid is empty there is nothing to compare to, simply return
        if (StringUtils.isEmpty(sid)) {
            return 0;
        }

        // first delete from the database in a separate transaction
        final int count = deleteSessionsBySid(sid);
        logger.info(format("SSO sessions deleted [%s] for sid [%s].", count, sid));

        // then delete all matching sessions from cache
        final List<String> keys = cache.asMap().entrySet().stream().filter(p -> sid.equals(p.getValue().getSid())).map(Map.Entry::getKey).collect(toList());
        cache.invalidateAll(keys);

        return count;
    }

    /**
     * This method is used strictly to enforce deletion in a separate transaction.
     *
     * @param sid
     * 
     * @return the number of deleted sessions
     */
    @SessionRequired(allowNestedScope = false)
    protected int deleteSessionsBySid(final String sid) {
        try {
            return this.defaultBatchDelete(select(UserSession.class).where().prop("sid").eq().val(sid).model());
        } catch (final Exception ex) {
            logger.error(format("Could not delete sessions by sid [%s].", sid), ex);
            return 0;
        }
    }

    /**
     * A helper method to remove sessions from cache.
     * 
     * @param q
     */
    private void invalidateCache(final EntityResultQueryModel<UserSession> q) {
        stream(from(q).with(fetchAll(UserSession.class)).model())
        .forEach(session -> cache.invalidate(findAuthenticator(session)));
    }

    /**
     * Removes all sessions that are associated with users that have been associated with the provided series ID. There should at most be only one such user (but potentially
     * multiple sessions) due to cryptographic randomness of series IDs.
     *
     * @param seriesId
     * @return
     * @throws SignatureException
     */
    @SessionRequired
    protected int removeSessionsForUsersBy(final String seriesId) {
        final EntityResultQueryModel<UserSession> query = select(UserSession.class).where().prop("seriesId").eq().val(seriesHash(seriesId)).model();
        final QueryExecutionModel<UserSession, EntityResultQueryModel<UserSession>> qem = from(query).with(fetchAll(UserSession.class)).model();

        // count the number of session to be removed...
        final int count = count(query);

        if (count > 0) {
            // get all those sessions and using their users, clear all sessions (there could more than the ones retrieved) associated with those users
            logger.warn(format("Removing specific session (%s) for all affected users due to a suspected stolen session authenticator.", count));
            stream(qem).forEach(ss -> clearAll(ss.getUser()));
        }

        return count;
    }

    /**
     * A convenient method for hashing the passed in series id.
     *
     * @param seriesId
     * @return
     */
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

    /**
     * The main goal of this method is to validate and refresh the user session, which happens for each request. The validation part has a very strong role for detecting fraudulent
     * attempts to access the system or indications for already compromised users that had their authenticators stolen.
     * <p>
     * If the presented authenticator is present in the cache then it is considered to be valid.
     * <p>
     * Please note that due to the fact that the first argument is a {@link User} instance, this means that the username has already been verified and identified as belonging to an
     * active user account.
     */
    @Override
    //@SessionRequired -- db session should not be used here
    public Optional<UserSession> currentSession(final User user0, final String authenticator, final boolean shouldConsiderTheftScenario, final boolean skipRegeneration) {
        final var user = new User();
        user.set("id", user0.getId());
        user.setKey(user0.getKey());

        // reconstruct authenticator from string and then proceed with its validation
        // in case of validation failure, no reason should be provided to the outside as this could reveal too much information to a potential adversary
        final Authenticator auth = fromString(authenticator);

        // check the cache first -- all authenicators in cache are considered valid
        // but just in case make sure that current user matches the authenticator user name
        final UserSession cachedSession = cache.getIfPresent(authenticator);
        if (cachedSession != null && user.getKey().equals(auth.username)) {
            return Optional.of(cachedSession);
        }

        // the authenticator is not in cache, a full authentication and series id regeneration process is in order
        // verify authenticator's authenticity using its hash and the application hashing key
        try {
            if (!auth.hash.equals(crypto.calculateRFC2104HMAC(auth.token, hashingKey))) {
                // authenticator has been tempered with
                logger.warn(format("The provided authenticator %s cannot be verified. A tempered authenticator is suspected.", auth));
                // remove user sessions that are identified with the provided series ID
                // series ID is the only possibly reliable piece of the authenticator at this stage as it is hard to forge algorithmically
                // therefore, in the worst case scenario that an authenticator has been stolen and series ID is being reused, all sessions for users that are associated with that series ID should be removed
                // because those users (should really be just one due to serial ID cryptographic randomness) have most likely been compromised
                // it is also possible that the provided series ID is also fraudulent and does not match any existing sessions, then no problems -- just deny the access and request explicit re-authentication
                final int count = removeSessionsForUsersBy(auth.seriesId);
                logger.debug(format("Removed %s session(s) for series ID %s", count, auth.seriesId));
                return Optional.empty();
            }

        } catch (final SignatureException ex) {
            final String msg = "Could not calculate a hash for session authenticator.";
            logger.error(msg, ex);
            throw new SecurityException(msg, ex);
        }

        // the provided authenticator has been verified based on its content and hash
        // this, however, does not guarantee that it has not been stolen and is not being used for illegal access to the system
        // the next logical thing to check is whether the user making the request and the user specified in the authenticator match
        if (!user.getKey().equals(auth.username)) {
            // authenticator has been stolen
            logger.warn(format("The provided authenticator %s does not reference the current user %s. A stolen authenticator is suspected.", auth, user.getKey()));
            // similarly as described previously, need to remove all sessions for user(s) that are associated with the series ID in the presented authenticator
            final int count = removeSessionsForUsersBy(auth.seriesId);
            logger.debug(format("Removed %s session(s) for series ID %s", count, auth.seriesId));
            return Optional.empty();
        }

        // so far so good, there is a hope that the current request is authentic, but there is still a chance that it is coming for an adversary...
        // let's find a persisted session, and there should be one if request is authentic, associated with the specified user and series ID
        final UserSession session = assignAuthenticator(findByKeyAndFetch(fetchAll(UserSession.class), user, seriesHash(auth.seriesId)), auth.seriesId);

        // if persisted session does not exist for a seemingly valid authenticator then it is most likely due to an authenticator theft, and here is why:
        // an authenticator could have been stolen, and already successfully used by an adversary to access the system from a different device than the one authenticator was stolen from
        // then, when a legitimate user is trying to access the system by presenting the stolen authenticator, which was already used by an adversary (this leads to series ID regeneration), then there would be no session associated with it!!!
        // this means all sessions for this particular user should be invalidated (removed) to stop an adversary from accessing the system
        if (session == null) {
            // before failing authentication need to check once again into the cache
            // to prevent false negative from occurring during concurrent requests from the same client
            try {
                Thread.sleep(200);
            } catch (final InterruptedException e) {
            }
            final UserSession us = cache.getIfPresent(authenticator);
            if (us != null) {
                return Optional.ofNullable(us);
            }

            // remove all user sessions if theft scenario should be considered
            if (shouldConsiderTheftScenario) {
                // if the session was not found in the cache then proceed with the theft story...
                logger.warn(format("A seemingly correct authenticator [%s] did not have a corresponding sesssion record.", auth));
                // in this case, sessions are removed based on user name and series ID, which is required taking into consideration that series ID could have been already regenerated
                final int count = clearAllFoUserAndBySeriesId(user, auth);
                logger.debug(format("Removed %s session(s) for series ID %s", count, auth.seriesId));
                return Optional.empty();
            } else { // otherwise just return an empty result, indicating no user session could be found
                logger.warn(format("A seemingly correct authenticator [%s] did not have a corresponding sesssion record, access denied (skip regeneration == %s).", auth, skipRegeneration));
                return Optional.empty();
            }
        }

        // only after we have a high probability for legitimate user request, the identified session needs to be check for expiration
        // for the time from the retrieved session should be used
        // in case of RSO, expired sessions should be invalidated and the request denied
        // in case of SSO (SID is present), expired sessions can still be re-validated by attempting to refresh the associated SID 
        final boolean sessionExpired = session.getExpiryTime().before(now().toDate());
        final boolean sessionRso = session.getSid() == null;
        if (sessionExpired && sessionRso) {
            logger.warn(format("Session for user [%s] has expired at [%s], access denied (skip regeneration == %s).", user, formatter.print(session.getExpiryTime().getTime()), skipRegeneration));
            // if authenticator has expired then use this opportunity to clear all expired sessions for the current
            clearExpired(user);
            ssoSessionController.invalidate(session.getSid());
            return empty();
        }

        // if this point is reached then the identified session is considered valid (almost, as it may still be an expired SSO session)
        // need to decide whether a new session should to be generated
        // if not then simply return the identified session back, even if it has expired
        // in practice this is limited to SSE requests that never return a response and thus would not be able to return a new security token back to the client
        if (skipRegeneration) {
            return of(session);
        }

        // otherwise, let's generate a new session
        try {
            // there is a tiny chance that there could be a clash of seriesId for the same user...
            // in this case, we may need to implement a re-try...
            // but let's first see if that is a problem by logging warning to this effect.
            final UserSession newSession = newSessionToReplaceOld(user, session.isTrusted(), of(authenticator), session.getSid());
            try {
                forceUpdateExpiryTimeForSession(session.getId(), user, now().plusMinutes(untrustedDurationMins));
            } catch (final Exception ex) {
                logger.info(format("Old session for user [%s] was not prolonged due to [%s].", user, ex.getMessage()));
            }

            final Result ssoRefreshed = ssoSessionController.refresh(session.getSid());
            if (!ssoRefreshed.isSuccessful()) {
                clearAllWithSid(session.getSid());
                ssoSessionController.invalidate(session.getSid());
                return empty();
            }

            return of(newSession);
        } catch (final Exception ex) {
            logger.warn(format("Saving of a new session for user [%s] did not succeed. Using previously verified session if not expired: [%s].", user, !sessionExpired), ex);
            return sessionExpired ? empty() : of(session);
        }
    }
    
    /**
     * Forcibly updates the expiry time for a session that is now replaced with a new one.
     * Forcibly means without regards for any concurrent modification, ignoring versioning to avoid conflict detection.
     *
     * @param oldSessionId
     * @param user
     * @param newExpiryTime
     */
    @SessionRequired(allowNestedScope = false)
    protected void forceUpdateExpiryTimeForSession(final Long oldSessionId, final User user, final DateTime newExpiryTime) {
        getSession().doWork(conn -> {
            try(final PreparedStatement ps = conn.prepareStatement("UPDATE USERSESSION_ SET EXPIRYTIME_ = ? WHERE _ID = ?")) {
                ps.setTimestamp(1, new java.sql.Timestamp(newExpiryTime.getMillis()));
                ps.setLong(2, oldSessionId);
                ps.executeUpdate();
            } catch (final Exception ex) {
                logger.warn(format("Could not update expiry time for old session for user [%s].", user), ex);
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
    public UserSession newSession(final User user0, final boolean isDeviceTrusted, final String sid) {
        final var user = new User();
        user.set("id", user0.getId());
        user.setKey(user0.getKey());

        return newSessionToReplaceOld(user, isDeviceTrusted, empty(), sid);
    }

    /**
     * Creates a new session, puts it into cache, and replaces a session associated with {@code oldAuthenticator} if it was provided.
     *
     * @param user
     * @param isDeviceTrusted
     * @param oldAuthenticator
     * @param sid 
     * @return
     */
    @SessionRequired
    protected UserSession newSessionToReplaceOld(final User user, final boolean isDeviceTrusted, final Optional<String> oldAuthenticator, final String sid) {
        // let's first construct the next series id
        final String seriesId = genSeriesId();
        final UserSession session = new_().setUser(user).setSeriesId(seriesHash(seriesId)).setSid(sid);
        
        session.setTrusted(isDeviceTrusted);
        final Date expiryTime = calcExpiryTime(isDeviceTrusted);
        session.setExpiryTime(expiryTime);
        session.setLastAccess(constants.now().toDate());

        // authenticator needs to be computed and assigned after the session has been persisted
        // assign authenticator in way not to disturb the entity meta-state
        final UserSession saved = assignAuthenticator(save(session), seriesId);

        // need to cache the established session as a plain object
        final UserSession userSessionForCache = saved.copyTo(newPlainEntity(UserSession.class, saved.getId()));
        userSessionForCache.setAuthenticator(saved.getAuthenticator().get());
        oldAuthenticator.ifPresent(auth -> cache.put(auth, userSessionForCache));
        cache.put(saved.getAuthenticator().get().toString(), userSessionForCache);
        return saved;
    }

    /**
     * A convenient method for generating and assigning an authenticator to {@code userSession}.
     *
     * @param userSession
     * @param seriesId
     */
    private UserSession assignAuthenticator(final UserSession userSession, final String seriesId) {
        // let's be a bit more defensive
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

    /**
     * A convenient method for instantiating an authenticator.
     *
     * @param user
     * @param seriesId
     * @param expiryTime
     * @return
     */
    @Override
    public Authenticator mkAuthenticator(final User user, final String seriesId, final long version, final Date expiryTime) {
        try {
            final String token = mkToken(user.getKey(), seriesId, version);
            final String hash = crypto.calculateRFC2104HMAC(token, hashingKey);
            return new Authenticator(Optional.of(expiryTime), token, hash);
        } catch (final SignatureException ex) {
            final String msg = "Could not make authneticator.";
            logger.error(msg, ex);
            throw new SecurityException(msg, ex);
        }
    }

    /**
     * A convenient method to find an authenticator key in cache by the user session value.
     * 
     * @param session
     * @return
     */
    private String findAuthenticator(final UserSession session) {
        for (final Entry<String, UserSession> value : cache.asMap().entrySet()) {
            if (value.getValue().equals(session)) {
                return value.getKey();
            }
        }
        return "";
    }
    
    /**
     * Calculates a session expiry time based on the notion of trusted and untrased devices.
     *
     * @param isDeviceTrusted
     * @return
     */
    private Date calcExpiryTime(final boolean isDeviceTrusted) {
        return (isDeviceTrusted ? constants.now().plusMinutes(trustedDurationMins) : constants.now().plusMinutes(untrustedDurationMins)).toDate();
    }

    public Cache<String, UserSession> getCache() {
        return cache;
    }
}