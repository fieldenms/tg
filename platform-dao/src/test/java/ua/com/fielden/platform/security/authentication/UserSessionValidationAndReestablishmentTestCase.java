package ua.com.fielden.platform.security.authentication;

import static org.junit.Assert.*;

import java.security.SignatureException;
import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case to cover user session management functionality.
 *
 * @author TG Team
 *
 */
public class UserSessionValidationAndReestablishmentTestCase extends AbstractDaoTestCase {

    private final IUserSession coSession = co(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);

    @Test
    public void current_user_should_have_a_valid_session_for_a_trusted_device_within_expiry_timeframe() throws SignatureException {
        // set the current time
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession newSession = coSession.newSession(currUser, true);
        final String authenticator = newSession.getAuthenticator().get().toString();

        // now let's move the clock 3 hours forward to emulate a time change and request a current session
        constants.setNow(dateTime("2015-04-23 16:00:00"));
        final Optional<UserSession> session = coSession.currentSession(currUser, authenticator);
        assertTrue(session.isPresent());
        assertEquals("Incorrect expiry time", constants.now().plusMinutes(60 * 24 * 3).getMillis(), session.get().getExpiryTime().getTime());
    }

    @Test
    public void current_user_should_not_have_a_valid_session_for_a_trusted_device_outside_expiry_timeframe() throws SignatureException {
        // set the current time
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession newSession = coSession.newSession(currUser, true);
        final String authenticator = newSession.getAuthenticator().get().toString();

        // now let's move the clock 5 days forward to emulate a time change and request a current session
        constants.setNow(dateTime("2015-04-28 13:00:00"));
        final Optional<UserSession> session = coSession.currentSession(currUser, authenticator);
        assertFalse(session.isPresent());
    }

    @Test
    public void current_user_should_have_a_valid_session_for_a_untrusted_device_within_expiry_timeframe() throws SignatureException {
        // set the current time
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession newSession = coSession.newSession(currUser, false);
        final String authenticator = newSession.getAuthenticator().get().toString();

        // now let's move the clock 2 minutes forward to emulate a time change and request a current session
        constants.setNow(dateTime("2015-04-23 13:02:00"));
        final Optional<UserSession> session = coSession.currentSession(currUser, authenticator);
        assertTrue(session.isPresent());
        assertEquals("Incorrect expiry time", constants.now().plusMinutes(5).getMillis(), session.get().getExpiryTime().getTime());
    }

    @Test
    public void current_user_should_not_have_a_valid_session_for_an_untrusted_device_outside_expiry_timeframe() throws SignatureException {
        // set the current time
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession newSession = coSession.newSession(currUser, false);
        final String authenticator = newSession.getAuthenticator().get().toString();

        // now let's move the clock more than 5 minutes forward to emulate a time change and request a current session
        constants.setNow(dateTime("2015-04-23 13:05:10"));
        final Optional<UserSession> session = coSession.currentSession(currUser, authenticator);
        assertFalse(session.isPresent());
    }


    @Test
    public void current_user_should_be_able_to_have_valid_trusted_and_expired_untrusted_sessions() throws SignatureException {
        // set the current time... the time when the user accessed the systems for the last time during the working day
        constants.setNow(dateTime("2015-04-23 17:26:00"));
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession trustedSession = coSession.newSession(currUser, true);
        final String trustedAuthenticator = trustedSession.getAuthenticator().get().toString();


        // early next morning, the user access the system from an untrusted tablet device on the way to work
        constants.setNow(dateTime("2015-04-24 07:30:00"));
        // establish a new session
        final UserSession untrustedSession = coSession.newSession(currUser, false);
        final String untrustedAuthenticator = untrustedSession.getAuthenticator().get().toString();


        // one hour later the user gets to work and tries to access the application from the trusted device
        constants.setNow(dateTime("2015-04-24 08:30:00"));
        final Optional<UserSession> renewdTrustedSession = coSession.currentSession(currUser, trustedAuthenticator);
        assertTrue(renewdTrustedSession.isPresent());
        // then during the morning coffee time, the user tries to use the same untrusted tablet that was used in the morning
        // and should be denied access, pending authentication
        constants.setNow(dateTime("2015-04-24 10:00:00"));
        final Optional<UserSession> failedSession = coSession.currentSession(currUser, untrustedAuthenticator);
        assertFalse(failedSession.isPresent());
    }

}