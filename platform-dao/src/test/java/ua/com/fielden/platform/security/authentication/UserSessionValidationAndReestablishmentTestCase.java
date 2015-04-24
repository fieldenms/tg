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

    private final IUserSession coSession = ao(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);

    @Test
    public void current_user_should_have_a_valid_session_for_a_trusted_device_within_expiry_timeframe() throws SignatureException {
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession newSession = coSession.newSession(currUser, true);
        final String authenticator = newSession.getAuthenticator().get().toString();

        // now let's move the clock 3 hours forward to emulate a time change and request a current session
        constants.setNow(dateTime("2015-04-23 16:00:00"));
        final Optional<UserSession> session = coSession.currentSession(currUser, authenticator);
        assertTrue(session.isPresent());
    }

    @Test
    public void current_user_should_not_have_a_valid_session_for_a_trusted_device_outside_expiry_timeframe() throws SignatureException {
        // establish a new session
        final User currUser = getInstance(IUserProvider.class).getUser();
        final UserSession newSession = coSession.newSession(currUser, true);
        final String authenticator = newSession.getAuthenticator().get().toString();

        // now let's move the clock 5 days forward to emulate a time change and request a current session
        constants.setNow(dateTime("2015-04-28 13:00:00"));
        final Optional<UserSession> session = coSession.currentSession(currUser, authenticator);
        assertFalse(session.isPresent());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2015-04-23 13:00:00"));
    }

}