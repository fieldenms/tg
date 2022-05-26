package ua.com.fielden.platform.security.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.SignatureException;
import java.util.Optional;

import org.junit.Test;

import com.google.common.base.Ticker;

import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.TickerForSessionCache;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case to cover very specific situation of a successfully stolen authenticator from a trusted device, which was reused by an adversary from
 * a different than stolen device, following a continuous use by a legitimate user.
 *
 * @author TG Team
 *
 */
public class UserSessionRecognisingStolenAndResuedAuthenticatorTestCase extends AbstractDaoTestCase {

    private final IUserSession coSession = co$(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final TickerForSessionCache cacheTicker = (TickerForSessionCache) getInstance(Ticker.class);

    @Test
    public void should_recognise_situation_with_stolen_and_used_authenticator_upon_a_legitimate_attempt_to_use_that_authenticator_by_valid_user() throws SignatureException {
        // establish a new sessions for user TEST
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(UNIT_TEST_USER, getInstance(IUser.class));
        final User currUser = getInstance(IUserProvider.class).getUser();

        constants.setNow(dateTime("2015-04-23 13:00:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 13:00:00"));
        final UserSession newSession = coSession.newSession(currUser, true, null);

        // authenticator that is associated with just established session gets stolen by an adversary....
        final String stolenAuthenticator = newSession.getAuthenticator().get().toString();

        // some time passes by....enough to evict the cached session
        constants.setNow(dateTime("2015-04-23 14:00:00"));

        // user attempts to use the stolen authenticator, which leads to a session refresh, basically invalidating the stolen authenticator by shortening its expiry time
        final Optional<UserSession> userSessionWithStolenAuthenticator = coSession.currentSession(currUser, stolenAuthenticator, false);
        assertTrue("User should have successfully accessed the system", userSessionWithStolenAuthenticator.isPresent());

        // more time passes, expiring the stolen authenticator, and adversary tries to access the system using that authenticator...
        // the session is not recognised as valid
        constants.setNow(dateTime("2015-04-23 14:07:00"));
        final Optional<UserSession> adversarySession = coSession.currentSession(currUser, stolenAuthenticator, false);
        assertFalse("Aversory should have been denied access.", adversarySession.isPresent());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        // add more users
        final IUser coUser = co$(User.class);
        save(new_(TgPerson.class, "Person 1").setUser(coUser.save(new_(User.class, "USER1").setBase(true).setEmail("USER1@unit-test.software").setActive(true))));
        save(new_(TgPerson.class, "Person 2").setUser(coUser.save(new_(User.class, "USER2").setBase(true).setEmail("USER2@unit-test.software").setActive(true))));

        // establish session for the above users
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("USER1", getInstance(IUser.class));
        final User user1 = up.getUser();

        // trusted session for USER1
        constants.setNow(dateTime("2015-04-23 16:26:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 16:26:00"));

        coSession.newSession(user1, true, null); // from work

        up.setUsername("USER2", getInstance(IUser.class));
        final User user2 = up.getUser();

        // trusted session for USER2
        constants.setNow(dateTime("2015-04-23 16:30:00"));
        coSession.newSession(user2, true, null); // from work
    }

}