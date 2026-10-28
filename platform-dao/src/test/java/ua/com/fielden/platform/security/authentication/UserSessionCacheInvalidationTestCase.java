package ua.com.fielden.platform.security.authentication;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.List;

import static org.junit.Assert.*;

/// A test case to cover the removal of cleared sessions from the session cache.
///
/// A session that replaces another one is cached for the authenticators of both, so that concurrent requests with either authenticator are accepted.
/// Clearing such a session removes it from cache for both authenticators, so that neither is accepted from cache afterwards.
///
public class UserSessionCacheInvalidationTestCase extends AbstractDaoTestCase {

    private static final String USERNAME = "USER1";

    private final UserSessionDao coSession = (UserSessionDao) co$(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);

    @Before
    public void startUp() {
        // Due to the global nature of the cache, it needs to be invalidated to keep tests independent.
        coSession.getCache().invalidateAll();
        constants.setNow(dateTime("2015-04-23 13:00:00"));
    }

    @Test
    public void clearing_a_session_that_replaced_another_one_removes_it_from_cache_for_both_authenticators() {
        final var authenticators = authenticatorsOfReplacedAndReplacingSessions();
        final String replacingAuthenticator = authenticators.getLast();

        // As on logout: the session is identified by its authenticator, and cleared.
        coSession.clearSession(coSession.currentSession(user(), replacingAuthenticator, true).orElseThrow());

        authenticators.forEach(authenticator -> assertNull("A cleared session should not be cached for authenticator [%s].".formatted(authenticator), coSession.getCache().getIfPresent(authenticator)));
        assertNotAccepted(replacingAuthenticator);
    }

    @Test
    public void clearing_a_session_keeps_other_sessions_of_the_user_cached_and_accepted() {
        final String replacingAuthenticator = authenticatorsOfReplacedAndReplacingSessions().getLast();
        // A session of the same user from another device or browser.
        final String otherAuthenticator = coSession.newSession(user(), false, null).getAuthenticator().orElseThrow().toString();

        // As on logout from the first device or browser.
        coSession.clearSession(coSession.currentSession(user(), replacingAuthenticator, true).orElseThrow());

        assertNotNull("The other session should remain cached.", coSession.getCache().getIfPresent(otherAuthenticator));
        assertTrue("The other session should remain accepted.", coSession.currentSession(user(), otherAuthenticator, true).isPresent());
    }

    @Test
    public void clearing_all_sessions_of_a_user_rejects_the_authenticators_of_a_session_that_replaced_another_one() {
        final var authenticators = authenticatorsOfReplacedAndReplacingSessions();

        coSession.clearAll(user());

        authenticators.forEach(this::assertNotAccepted);
    }

    @Test
    public void resetting_the_password_of_a_user_rejects_the_authenticators_of_a_session_that_replaced_another_one() {
        final var authenticators = authenticatorsOfReplacedAndReplacingSessions();

        final IUser coUser = co$(User.class);
        coUser.resetPasswd(coUser.findUser(USERNAME), "a strong password for USER1");

        authenticators.forEach(this::assertNotAccepted);
    }

    @Test
    public void deactivating_a_user_rejects_the_authenticators_of_a_session_that_replaced_another_one() {
        final var authenticators = authenticatorsOfReplacedAndReplacingSessions();

        final IUser co$User = co$(User.class);
        co$User.save(co$User.findByKey(USERNAME).setActive(false));

        authenticators.forEach(this::assertNotAccepted);
    }

    /// Establishes a session and replaces it with a new one, as on a request after the session is evicted from cache.
    /// Returns the authenticators of the replaced session and of the replacing one, in that order, both of which are cached for the replacing session.
    ///
    private List<String> authenticatorsOfReplacedAndReplacingSessions() {
        final String replacedAuthenticator = coSession.newSession(user(), false, null).getAuthenticator().orElseThrow().toString();

        coSession.getCache().invalidateAll();
        constants.setNow(dateTime("2015-04-23 13:01:00"));
        final UserSession replacingSession = coSession.currentSession(user(), replacedAuthenticator, false).orElseThrow();
        final String replacingAuthenticator = replacingSession.getAuthenticator().orElseThrow().toString();

        assertEquals("Invalid initial conditions: the replacing session should be cached for both authenticators.",
                     List.of(replacingSession, replacingSession),
                     List.of(coSession.getCache().getIfPresent(replacedAuthenticator), coSession.getCache().getIfPresent(replacingAuthenticator)));
        return List.of(replacedAuthenticator, replacingAuthenticator);
    }

    private void assertNotAccepted(final String authenticator) {
        assertNull("A cleared session should not be cached for authenticator [%s].".formatted(authenticator), coSession.getCache().getIfPresent(authenticator));
        assertTrue("Authenticator [%s] should not be accepted.".formatted(authenticator), coSession.currentSession(user(), authenticator, true).isEmpty());
    }

    private User user() {
        return this.<IUser, User>co(User.class).findUser(USERNAME);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        co$(User.class).save(new_(User.class, USERNAME).setBase(true).setEmail("USER1@unit-test.software").setActive(true));
    }

}
