package ua.com.fielden.platform.security.session;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.security.session.Authenticator.mkToken;

import java.security.SignatureException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.IUniversalConstants;

import com.google.inject.Inject;

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
    private final IUniversalConstants constants;

    @Inject
    public UserSessionDao(
            final @SessionHashingKey String hashingKey,
            final @TrustedDeviceSessionDuration int trustedDurationMins,
            final @UntrustedDeviceSessionDuration int untrustedDurationMins,
            final IUniversalConstants constants,
            final SessionIdentifierGenerator crypto,
            final IFilter filter) {
        super(filter);
        this.constants = constants;
        this.hashingKey = hashingKey;
        this.trustedDurationMins = trustedDurationMins;
        this.untrustedDurationMins = untrustedDurationMins;
        this.crypto = crypto;
    }

    @Override
    public void clearAll(final User user) {
        super.defaultDelete(select(UserSession.class).where().prop("user").eq().val(user).model());
    }

    @Override
    public void clearUntrusted(final User user) {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                        .where()
                        .prop("user").eq().val(user)
                        .and().prop("trusted").eq().val(false)
                        .model();
        super.defaultDelete(query);
    }

    @Override
    public void clearAll() {
        super.defaultDelete(select(UserSession.class).model());

    }

    @Override
    public void clearUntrusted() {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                        .where()
                        .prop("trusted").eq().val(false)
                        .model();
        super.defaultDelete(query);
    }

    @Override
    public void clearExpired(final User user) {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                        .where()
                        .prop("user").eq().val(user)
                        .and().prop("expiryTime").lt().now()
                        .model();
        super.defaultDelete(query);
    }

    @Override
    public void clearExpired() {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                        .where()
                        .prop("expiryTime").lt().now()
                        .model();
        super.defaultDelete(query);
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
            final List<UserSession> all = getAllEntities(qem);
            final String users = all.stream().map(ss -> ss.getUser().getKey()).distinct().collect(Collectors.joining(", "));
            logger.warn(format("Removing all sessions for user(s) %s due to a suspected stolen session authenticator.", users));
            all.forEach(ss -> clearAll(ss.getUser()));
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
     * The main goal of this method is to validate and refresh the user session, which happens for each request.
     * The validation part has a very strong role for detecting fraudulent attempts to access the system or indications for already compromised users that had their authenticators stolen.
     * <p>
     * Please note that due to the fact that the first argument is a {@link User} instance, this
     * means that the username has already been verified and identified as belonging to an active user account.
     */
    @Override
    @SessionRequired
    public Optional<UserSession> currentSession(final User user, final String authenticator) {
        // first reconstruct authenticator from string and then proceed with its validation
        // in case of validation failure, no reason should be provided to the outside as this could reveal too much information to a potential adversary
        final Authenticator auth = fromString(authenticator);

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

        // so far so good, there is a hope that the current request is authentic, but there is still a chance that it is coming for an adversary...
        // let's find a persisted session, and there should be one if request is authentic, associated with the specified user and series ID
        final UserSession session = findByKeyAndFetch(fetchAll(UserSession.class), user, seriesHash(auth.seriesId));
        // if persisted session does not exist for a seemingly valid authenticator then it is most likely due to an authenticator theft, and here is why:
        // an authenticator could have been stolen, and already successfully used by an adversary to access the system from a different device than the one authenticator was stolen from
        // then, when a legitimate user is trying to access the system by presenting the stolen authenticator, which was already used be an adversary (this leads to series ID regeneration), then there would be no session associated with it!!!
        // this means all sessions for this particular user should be invalidated (removed) to stop an adversary from accessing the system
        if (session == null) {
            logger.warn(format("A seemingly correct authenticator %s did not have a corresponding sesssion record. An authenticator theft is suspected. An adversary might have had access to the system as user %s", auth, user.getKey()));
            final int count = removeSessionsForUsersBy(auth.seriesId);
            logger.debug(format("Removed %s session(s) for series ID %s", count, auth.seriesId));
            return Optional.empty();
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
        // now let's check if session has expired, if it did then use this opportunity to clear all expired session for this user and return an empty result to trigger re-authentication
        if (auth.getExpiryTime().isBefore(constants.now().getMillis())) {
            // clean up expired sessions
            clearExpired(user);
            return Optional.empty();
        }

        // if this point is reached then the identified session is considered valid
        // it needs to be updated with a new series ID and a refreshed expiry time, and returned as the result
        final String seriesId = crypto.nextSessionId();
        session.setSeriesId(seriesHash(seriesId));
        session.setLastAccess(constants.now().toDate());
        final Date expiryTime = calcExpiryTime(session.isTrusted());
        session.setExpiryTime(expiryTime);
        final UserSession updated = save(session);
        // assign authenticator, but in way not to disturb the entity meta-state
        updated.beginInitialising();
        updated.setAuthenticator(mkAuthenticator(user, seriesId, expiryTime));
        updated.endInitialising();

        return Optional.of(updated);
    }

    @Override
    @SessionRequired
    public UserSession newSession(final User user, final boolean isDeviceTrusted) {
        try {
            // let's first construct the next series id
            final String seriesId = crypto.nextSessionId();
            // and hash it for storage
            final String seriesIdHash = crypto.calculateRFC2104HMAC(seriesId, hashingKey);

            final UserSession session = user.getEntityFactory().newByKey(UserSession.class, user, seriesIdHash);
            session.setTrusted(isDeviceTrusted);
            final Date expiryTime = calcExpiryTime(isDeviceTrusted);
            session.setExpiryTime(expiryTime);
            session.setLastAccess(constants.now().toDate());

            // authenticator needs to be computed and assigned after the session has been persisted
            // assign authenticator in way not to disturb the entity meta-state
            final UserSession saved = save(session);
            saved.beginInitialising();
            saved.setAuthenticator(mkAuthenticator(user, seriesId, expiryTime));
            saved.endInitialising();

            return saved;

        } catch (final SignatureException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
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
     * Calculates a session expiry time based on the notion of trusted and untrased devices.
     *
     * @param isDeviceTrusted
     * @return
     */
    private Date calcExpiryTime(final boolean isDeviceTrusted) {
        return (isDeviceTrusted ? constants.now().plusMinutes(trustedDurationMins) : constants.now().plusMinutes(untrustedDurationMins)).toDate();
    }
}