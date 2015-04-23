package ua.com.fielden.platform.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.security.SignatureException;

import org.junit.Test;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

import com.google.inject.Inject;

/**
 * A test case to cover user session creation.
 *
 * @author TG Team
 *
 */
public class UserSessionEstablishmentTestCase extends AbstractDaoTestCase {

    private final IUserSession coSession = ao(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);


    @Test
    public void new_session_for_trusted_device_should_be_created_succesfully() throws SignatureException {
        final User currUser = getInstance(IUserProvider.class).getUser();
        assertNotNull(currUser);

        final UserSession session = coSession.newSession(currUser, true);
        assertNotNull("User session should have been created.", session);

        assertTrue(session.isPersisted());
        assertEquals(constants.now().toDate(), session.getLastAccess());
        assertTrue(session.getAuthenticator().isPresent());
        assertNotNull(session.getUser());
        assertNotNull(session.getSeriesId());
        assertNotNull(session.getExpiryTime());
        assertTrue(session.isTrusted());

        final SessionParams params = getInstance(SessionParams.class);

        assertEquals(params.crypto.calculateRFC2104HMAC(session.getAuthenticator().get().token, params.hashingKey), session.getAuthenticator().get().hash);
        assertEquals(constants.now().plusMinutes(60 * 24 * 3), session.getAuthenticator().get().getExpiryTime());
    }

    @Test
    public void new_session_for_untrusted_device_should_be_created_succesfully() throws SignatureException {
        final User currUser = getInstance(IUserProvider.class).getUser();
        assertNotNull(currUser);

        final UserSession session = coSession.newSession(currUser, false);
        assertNotNull("User session should have been created.", session);

        assertTrue(session.isPersisted());
        assertEquals(constants.now().toDate(), session.getLastAccess());
        assertTrue(session.getAuthenticator().isPresent());
        assertNotNull(session.getUser());
        assertNotNull(session.getSeriesId());
        assertNotNull(session.getExpiryTime());
        assertFalse(session.isTrusted());

        final SessionParams params = getInstance(SessionParams.class);

        assertEquals(params.crypto.calculateRFC2104HMAC(session.getAuthenticator().get().token, params.hashingKey), session.getAuthenticator().get().hash);
        assertEquals(constants.now().plusMinutes(5), session.getAuthenticator().get().getExpiryTime());
    }

    @Test
    public void a_sigle_user_can_have_multiple_sessions_from_trusted_and_untrusted_devices() throws SignatureException {
        final User currUser = getInstance(IUserProvider.class).getUser();
        assertNotNull(currUser);

       coSession.newSession(currUser, false);
       coSession.newSession(currUser, true);
       coSession.newSession(currUser, false);
       coSession.newSession(currUser, true);

       assertEquals(4, coSession.count(select(UserSession.class).where().prop("user").eq().val(currUser). model()));
    }

    /**
     * Domain state population method.
     */
    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2015-04-23 13:00:00"));
    }

    /**
     * This is just a convenience class to capture session creation parameters for testing purposes.
     *
     */
    public static class SessionParams {
        final String hashingKey;
        final int trustedDurationMins;
        final int untrustedDurationMins;
        final SessionIdentifierGenerator crypto;

        @Inject
        public SessionParams(
                final @SessionHashingKey String hashingKey,
                final @TrustedDeviceSessionDuration int trustedDurationMins,
                final @UntrustedDeviceSessionDuration int untrustedDurationMins,
                final SessionIdentifierGenerator crypto) {
            this.hashingKey = hashingKey;
            this.trustedDurationMins = trustedDurationMins;
            this.untrustedDurationMins = untrustedDurationMins;
            this.crypto = crypto;
        }

    }

}