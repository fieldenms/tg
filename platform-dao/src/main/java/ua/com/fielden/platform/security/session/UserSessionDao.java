package ua.com.fielden.platform.security.session;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.security.session.Authenticator.AUTHENTICATOR_SEPARATOR;
import static ua.com.fielden.platform.security.session.Authenticator.mkToken;

import java.security.SignatureException;
import java.util.Date;
import java.util.Optional;

import org.joda.time.DateTime;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.dao.CommonEntityDao;
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

    @Override
    @SessionRequired
    public Optional<UserSession> currentSession(final User user, final String authenticator) {


        // separate username from the encoded part of the token
        final String[] parts = authenticator.split(AUTHENTICATOR_SEPARATOR);
        if (parts.length != 4) {
        }
        // use the username to lookup a corresponding public key to decode security token
        final String username = parts[0];
        final String seriesId = parts[1];
        final String expiryTimeStr = parts[2];
        // should be used to check authenticator expiration
        final DateTime expiryTime = new DateTime(Long.parseLong(expiryTimeStr));
        final String hashCode = parts[3];

        final String token = username + seriesId + expiryTimeStr;
        try {
            final String computedHash = crypto.calculateRFC2104HMAC(token, hashingKey);
        } catch (final SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO Auto-generated method stub
        return null;
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
            final Date expiryTime  = (isDeviceTrusted ? constants.now().plusMinutes(trustedDurationMins) : constants.now().plusMinutes(untrustedDurationMins)).toDate();
            session.setExpiryTime(expiryTime);
            session.setLastAccess(constants.now().toDate());

            // authenticator needs to be computed and assigned after the session has been persisted
            final String token = mkToken(user.getKey(), seriesId, expiryTime);
            final String hash = crypto.calculateRFC2104HMAC(token, hashingKey);
            return save(session).setAuthenticator(new Authenticator(token, hash));

        } catch (final SignatureException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

}