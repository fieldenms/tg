package ua.com.fielden.platform.security.session;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.security.session.Authenticator.mkToken;

import java.security.SignatureException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.cache.Cache;
import com.google.inject.Inject;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * DAO implementation for companion object {@link IUserSession}.
 *
 * @author Developers
 *
 */
@EntityType(UserSession.class)
public class UserSessionDao extends CommonEntityDao<UserSession> implements IUserSession {
    private final Logger logger = Logger.getLogger(UserSessionDao.class);

    /** A key to be used for hashing authenticators and series ID before storing them. */
    private final String hashingKey;
    private final int trustedDurationMins;
    private final int untrustedDurationMins;
    private final SessionIdentifierGenerator crypto;
    private final Cache<String, UserSession> cache;
    private final IUniversalConstants constants;

    @Inject
    public UserSessionDao(
            final @SessionHashingKey String hashingKey,
            final @TrustedDeviceSessionDuration int trustedDurationMins,
            final @UntrustedDeviceSessionDuration int untrustedDurationMins,
            final @SessionCache Cache<String, UserSession> cache,
            final IUniversalConstants constants,
            final SessionIdentifierGenerator crypto,
            final IFilter filter) {
        super(filter);
        this.constants = constants;
        this.hashingKey = hashingKey;
        this.trustedDurationMins = trustedDurationMins;
        this.untrustedDurationMins = untrustedDurationMins;
        this.crypto = crypto;
        this.cache = cache;
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
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                        .where()
                        .prop("user").eq().val(user)
                        .and().prop("expiryTime").lt().now()
                        .model();
        
        invalidateCache(query);
        
        defaultBatchDelete(query);
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
    private int removeSessionsForUsersBy(final String seriesId) {
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
            throw new IllegalStateException(ex);
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
    @SessionRequired
    public Optional<UserSession> currentSession(final User user, final String authenticator, final boolean shouldConsiderTheftScenario) {
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
            throw new IllegalStateException(ex);
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

        // the presented authenticator might already be expired and does not need to be validated any further -- simply return no session
        // if authenticator has expired then use this opportunity to clear all expired sessions for the current
        if (auth.getExpiryTime().isBefore(constants.now().getMillis())) {
            logger.warn(format("The provided authenticator %s for user %s has expired.", auth, user.getKey()));
            // clean up expired sessions
            clearExpired(user);
            return Optional.empty();
        }

        // so far so good, there is a hope that the current request is authentic, but there is still a chance that it is coming for an adversary...
        // let's find a persisted session, and there should be one if request is authentic, associated with the specified user and series ID
        final UserSession session = findByKeyAndFetch(fetchAll(UserSession.class), user, seriesHash(auth.seriesId));
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
                logger.warn(format("A seemingly correct authenticator %s did not have a corresponding sesssion record. An authenticator theft is suspected. An adversary might have had access to the system as user %s", auth, user.getKey()));
                // in this case, sessions are removed based on user name and series ID, which is required taking into consideration that series ID could have been already regenerated
                final int count = clearAll(user) + removeSessionsForUsersBy(auth.seriesId);
                logger.debug(format("Removed %s session(s) for series ID %s", count, auth.seriesId));
                return Optional.empty();
            } else { // otherwise just return an empty result, indicating no user session could be found
                return Optional.empty();
            }
        }

        // only after we have a high probability for legitimate user request, the identified session needs to be check for expiry
        // for this either the authenticator's time portion or the time from the retrieved session could be used
        // but to provide one additional level of session verification, let's make sure that both times are identical
        // if they are not.... then most likely the database was tempered with... just log the problem, but proceed with further session validation
        // the thing is that the only way to reach this point of validation for an authenticator, it would need to be either valid or an adversary would needed to either steal it and no legitimate user yet used it, or forge it, which would
        // require an access to the server... in this unfortunate case there is no way to identify the actually stolen session...
        if (auth.expiryTime != session.getExpiryTime().getTime()) {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
            logger.warn(format("Session expiry time %s for authenticator %s differes to the persisted expiry time %s.", formatter.print(auth.expiryTime), auth, formatter.print(session.getExpiryTime().getTime())));
        }

        // if this point is reached then the identified session is considered valid
        // it needs to be updated with a new series ID and a refreshed expiry time, and returned as the result
        final String seriesId = crypto.nextSessionId();
        session.setSeriesId(seriesHash(seriesId));
        session.setLastAccess(constants.now().toDate());
        final Date expiryTime = calcExpiryTime(session.isTrusted());
        session.setExpiryTime(expiryTime);

        // due to potentially highly concurrent requests from the same web client upon a refresh after some stale period where cache has already been evicted
        // saving of the newly created session may fail due to concurrent update
        // therefore, in case the save call fails, we hope that an updated session has already been placed into the cache by a concurrent process
        try {
            final UserSession updated = save(session);
            // assign authenticator, but in way not to disturb the entity meta-state
            updated.beginInitialising();
            updated.setAuthenticator(mkAuthenticator(updated.getUser(), seriesId /* un-hashed */, updated.getExpiryTime()));
            updated.endInitialising();

            // in order to support concurrent request from the same user it is necessary to
            // associate the presented and verified authenticator as well as the new authenticator with an updated session in the session cache
            final String newAuthenticator = updated.getAuthenticator().get().toString();
            cache.put(authenticator, updated);
            cache.put(newAuthenticator, updated);

            return Optional.of(updated);
        } catch (final Exception e) {
            logger.warn(e);
            logger.debug(format("Saving of a new session for user %s has failed due to concurrent update. Trying to recover a session from cache...", user.getKey()));
            final UserSession us = cache.getIfPresent(authenticator);
            logger.debug(format("Session recovery for user %s was successful: %s", user.getKey(), (us != null)));
            return Optional.ofNullable(us);
        }
    }

    @Override
    public final Optional<UserSession> currentSession(final User user, final String authenticator) {
        return currentSession(user, authenticator, true);
    }

    @Override
    @SessionRequired
    public UserSession newSession(final User user, final boolean isDeviceTrusted) {
        // let's first construct the next series id
        final String seriesId = crypto.nextSessionId();
        final UserSession session = user.getEntityFactory().newByKey(UserSession.class, user, seriesHash(seriesId));
        session.setTrusted(isDeviceTrusted);
        final Date expiryTime = calcExpiryTime(isDeviceTrusted);
        session.setExpiryTime(expiryTime);
        session.setLastAccess(constants.now().toDate());

        // authenticator needs to be computed and assigned after the session has been persisted
        // assign authenticator in way not to disturb the entity meta-state
        final UserSession saved = save(session);
        saved.beginInitialising();
        saved.setAuthenticator(mkAuthenticator(saved.getUser(), seriesId /* un-hashed */, saved.getExpiryTime()));
        saved.endInitialising();

        // need to cache the established session in associated with the generated authenticator
        cache.put(saved.getAuthenticator().get().toString(), saved);

        return saved;
    }

    /**
     * A convenient method for instantiating an authenticator.
     *
     * @param user
     * @param seriesId
     * @param expiryTime
     * @return
     */
    private Authenticator mkAuthenticator(final User user, final String seriesId, final Date expiryTime) {
        try {
            final String token = mkToken(user.getKey(), seriesId, expiryTime);
            final String hash = crypto.calculateRFC2104HMAC(token, hashingKey);
            return new Authenticator(token, hash);
        } catch (final SignatureException ex) {
            throw new IllegalStateException(ex);
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