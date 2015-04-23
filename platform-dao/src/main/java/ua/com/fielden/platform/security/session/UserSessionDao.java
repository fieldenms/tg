package ua.com.fielden.platform.security.session;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.security.session.Authenticator.mkToken;

import java.security.SignatureException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
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
     * Removes all user session by series ID. There should at most be only one such session due to cryptographic randomness of series IDs.
     *
     * @param seriesId
     * @return
     * @throws SignatureException
     */
    private int removeSessionsBy(final String seriesId) {
        final EntityResultQueryModel<UserSession> query = select(UserSession.class).where().prop("seriesId").eq().val(seriesHash(seriesId)).model();

        final int count = count(query);
        if (count > 0) {
            super.defaultDelete(query);
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

    @Override
    @SessionRequired
    public Optional<UserSession> currentSession(final User user, final String authenticator) {
        // first reconstruct authenticator from string and then proceed with its validation
        // in case of validation failure, no reason should be provided to the outside as this could reveal too much information to a potential adversary
        final Authenticator auth = fromString(authenticator);

        // most importantly verify authenticator's authenticity using its hash
        try {
            if (!auth.hash.equals(crypto.calculateRFC2104HMAC(auth.token, hashingKey))) {
                // authenticator has been tempered with
                // remove sessions associated with the provided series id if any
                final int count = removeSessionsBy(auth.seriesId);
                logger.debug(format("Removed %s session(s) for series ID %s", count, auth.seriesId));
                return Optional.empty();
            }
        } catch (final SignatureException ex) {
            throw new IllegalStateException(ex);
        }

        // make sure the provided authenticator is for the declared user
        if (!user.getKey().equals(auth.username)) {
            // authenticator has been stolen
            // remove sessions associated with the provided series id if any
            final int count = removeSessionsBy(auth.seriesId);
            logger.debug(format("Removed %s session(s) for series ID %s", count, auth.seriesId));
            return Optional.empty();
        }

        // has authenticator expired?
        if (auth.getExpiryTime().isBefore(constants.now().getMillis())) {
            // clean up expired sessions
            clearExpired(user);
            return Optional.empty();
        }

        // try to get the persisted session associated with the specified user and series ID
        final UserSession session = findByKeyAndFetch(fetchAll(UserSession.class), user, seriesHash(auth.seriesId));
        // this is an extremely unlikely scenario, but to be on a defensive side...
        if (session == null) {
            logger.warn(format("A seemingly correct authenticator for user %s and series ID %s did not have a corresponding sesssion record.", user.getKey(), auth.seriesId));

            // try to identify users to whom series ID might belong, should really be at most one
            final EntityResultQueryModel<UserSession> query = select(UserSession.class).where().prop("seriesId").eq().prop(auth.seriesId).model();
            final QueryExecutionModel<UserSession, EntityResultQueryModel<UserSession>> qem = from(query).with(fetchAll(UserSession.class)).model();

            final List<UserSession> all = getAllEntities(qem);
            if (all.size() > 0) {
                final String users = all.stream().map(ss -> ss.getUser().getKey()).distinct().collect(Collectors.joining(", "));
                logger.warn(format("Removing all sessions for user(s) %s due to suspected stolen session authenticators.", users));
                all.forEach(ss -> clearAll(ss.getUser()));
            }
            return Optional.empty();
        }

        // if this point is reached then the presented
        // let's update the session with a new series ID and return it as a result
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