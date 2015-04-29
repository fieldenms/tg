package ua.com.fielden.platform.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.SignatureException;
import java.util.Optional;
import java.util.Random;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.TickerForSessionCache;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;

/**
 * A test case to cover very specific situation of a successfully stolen authenticator from a trusted device, which was reused by an adversary from a different than stolen device,
 * followed by an attempt to be used by a legitimate user.
 *
 * @author TG Team
 *
 */
public class UserSessionCacheEvictionStrategyTestCase extends AbstractDaoTestCase {

    private final UserSessionDao coSession = (UserSessionDao) ao(UserSession.class);
    private final Cache<String, UserSession> cache = coSession.getCache();
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final TickerForSessionCache cacheTicker = (TickerForSessionCache) getInstance(Ticker.class);

    @Before
    public void startUp() {
        // due to global cache nature it needs to be invalidated in order to keep tests independent
        cache.invalidateAll();
    }

    @Test
    public void cache_should_contain_exactly_one_session_after_a_single_successful_login() throws SignatureException {
        // establish a new sessions for user TEST, which effectively emulates the explicit login
        final User currUser = getInstance(IUserProvider.class).getUser();
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 13:00:00"));
        final UserSession newSession = coSession.newSession(currUser, true);

        // at this stage the cache should contain only one entry
        assertEquals(1, coSession.getCache().size());
        assertNotNull(coSession.getCache().getIfPresent(newSession.getAuthenticator().get().toString()));
    }

    @Test
    public void should_permit_burst_requests_from_the_same_trusted_device_immeditely_afte_login_all_within_eviction_time() throws SignatureException {
        // establish a new sessions for user TEST, which effectively emulates the explicit login
        final User currUser = getInstance(IUserProvider.class).getUser();
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 13:00:00"));
        final UserSession newSession = coSession.newSession(currUser, true);
        final String newAuthenticator = newSession.getAuthenticator().get().toString();

        // emulate burst requests with random increment of request time within 2 seconds
        final Random rnd = new Random();
        DateTime requestTime = dateTime("2015-04-23 13:00:02");
        for (int index = 0; index < 10; index++) {
            final int millisInc = rnd.nextInt(2000 - 500 + 1) + 500; // a most 2000 and at least 500 milliseconds
            requestTime = new DateTime(requestTime.getMillis() + millisInc);
            constants.setNow(requestTime);
            final Optional<UserSession> session = coSession.currentSession(currUser, newAuthenticator);
            assertTrue(session.isPresent());
            assertEquals("Authenticator should not have been reset", newAuthenticator, session.get().getAuthenticator().get().toString());
            assertEquals(1, coSession.getCache().size());
            assertNotNull(coSession.getCache().getIfPresent(newAuthenticator));
        }
    }

    @Test
    public void should_permit_burst_requests_from_the_same_untrusted_device_immeditely_afte_login_all_within_eviction_time() throws SignatureException {
        // establish a new sessions for user TEST, which effectively emulates the explicit login
        final User currUser = getInstance(IUserProvider.class).getUser();
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 13:00:00"));
        final UserSession newSession = coSession.newSession(currUser, false);
        final String newAuthenticator = newSession.getAuthenticator().get().toString();

        // emulate burst requests with random increment of request time within 2 seconds
        final Random rnd = new Random();
        DateTime requestTime = dateTime("2015-04-23 13:00:02");
        for (int index = 0; index < 10; index++) {
            final int millisInc = rnd.nextInt(2000 - 500 + 1) + 500; // a most 2000 and at least 500 milliseconds
            requestTime = new DateTime(requestTime.getMillis() + millisInc);
            constants.setNow(requestTime);
            final Optional<UserSession> session = coSession.currentSession(currUser, newAuthenticator);
            assertTrue(session.isPresent());
            assertEquals("Authenticator should not have been reset.", newAuthenticator, session.get().getAuthenticator().get().toString());
            assertEquals(1, coSession.getCache().size());
            assertNotNull(coSession.getCache().getIfPresent(newAuthenticator));
        }

    }


    @Override
    protected void populateDomain() {
        super.populateDomain(); // creates the default current user TEST

        constants.setNow(dateTime("2015-04-23 15:00:00"));

        // add more users
        save(new_(TgPerson.class, "Person 1").setUsername("USER-1").setBase(true));
        save(new_(TgPerson.class, "Person 2").setUsername("USER-2").setBase(true));

        // ensure that TEST is the current user
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("TEST", getInstance(IUserController.class));
    }

}