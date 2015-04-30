package ua.com.fielden.platform.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.security.SignatureException;
import java.util.Optional;
import java.util.Random;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
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
 * A test case to cover authenticator revalidation in case of evicted sessions and recognition of stolen authenticators that takes into account the eviction strategy.
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
    public void should_permit_burst_requests_from_the_same_trusted_device_immeditely_afte_login_all_within_eviction_time() {
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
    public void should_permit_burst_requests_from_the_same_untrusted_device_immeditely_afte_login_all_within_eviction_time() {
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

    /**
     * This test simulates the situation where an evicted authenticator gets presented,
     * successfully validated, regenerated, but followed by request with the original authenticator, which were send
     * from the client prior to receiving this regenerated authenticator.
     * Such requests should be recognised as authentic and permitted.
     */
    @Test
    public void should_permit_reuse_of_verified_but_evicted_and_regenerated_authenticator_for_duration_of_eviction() {
        // establish a new sessions for user TEST, which effectively emulates the explicit login
        final User currUser = getInstance(IUserProvider.class).getUser();
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 13:00:00"));
        final UserSession newSession = coSession.newSession(currUser, true);
        final String authenticator = newSession.getAuthenticator().get().toString();

        // enough time has passed to evict authenticators from cache
        constants.setNow(dateTime("2015-04-23 13:06:00"));

        // try to get the current session using the evicted, but valid authenticator
        // this should lead to creation of a new authenticator and registration of a new session
        // that would be associated with the presented and new authenticators in cache
        final Optional<UserSession> session = coSession.currentSession(currUser, authenticator);
        assertTrue(session.isPresent());
        final String newAuthenticator = session.get().getAuthenticator().get().toString();
        assertNotSame("Authenticator should have been reset.", authenticator, newAuthenticator);
        assertEquals("Unexpected number of session in cache.", 2, coSession.getCache().size());
        assertNotNull("Original authenticator should be present in cache.", coSession.getCache().getIfPresent(authenticator));
        assertNotNull("New authenticator should be present in cache.", coSession.getCache().getIfPresent(newAuthenticator));
        assertSame("Original and new authenticators should point to the same session instance.", coSession.getCache().getIfPresent(authenticator), coSession.getCache().getIfPresent(newAuthenticator));

        // the first request that presented the original and now regenerated authenticator happen to be followed by another request(s)
        // from the same device carrying the same original authenticator
        // this is because the regenerated authenticator did not get back to the device yet
        // such subsequent request(s) should still be accepted as authentic!
        final Random rnd = new Random();
        DateTime requestTime = dateTime("2015-04-23 13:06:01");
        for (int index = 0; index < 10; index++) {
            final int millisInc = rnd.nextInt(2000 - 500 + 1) + 500; // a most 2000 and at least 500 milliseconds
            requestTime = new DateTime(requestTime.getMillis() + millisInc);
            constants.setNow(requestTime);
            // the original authenticator is presented!
            final Optional<UserSession> ss = coSession.currentSession(currUser, authenticator);
            assertTrue(ss.isPresent());
            assertEquals("A regenerated authentication is expected, which ensures that responses provide the latest authenticator.", newAuthenticator, ss.get().getAuthenticator().get().toString());
            assertEquals(2, coSession.getCache().size());
            assertNotNull("Original authenticator should be present in cache.", coSession.getCache().getIfPresent(authenticator));
            assertNotNull("New authenticator should be present in cache.", coSession.getCache().getIfPresent(newAuthenticator));
            assertSame("Original and new authenticators should point to the same session instance.", coSession.getCache().getIfPresent(authenticator), coSession.getCache().getIfPresent(newAuthenticator));
        }
    }

    /**
     * This test simulates the situation where an evicted authenticator gets presented,
     * successfully validated and regenerated.
     * The original authenticator should remain recognisable until the eviction time passes, which is tested in
     * {@link #should_permit_reuse_of_verified_but_evicted_and_regenerated_authenticator_for_duration_of_eviction()}.
     * <p>
     * However, if the same original authenticator is presented again after the eviction time passes then it should be recognised as invalid (most likely stolen)!
     */
    @Test
    public void should_not_permit_reauthentication_of_regenerated_and_evicted_authenticator_correctly_recognising_authenticator_theft() throws SignatureException {
        // establish a new sessions for user TEST, which effectively emulates the explicit login
        final User currUser = getInstance(IUserProvider.class).getUser();
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 13:00:00"));
        final UserSession newSession = coSession.newSession(currUser, true);
        final String authenticator = newSession.getAuthenticator().get().toString();

        // enough time has passed to evict authenticators from cache
        constants.setNow(dateTime("2015-04-23 13:06:00"));

        // emulate a new request, which presents valid, but evicted authenticator
        final Optional<UserSession> session = coSession.currentSession(currUser, authenticator);
        assertTrue(session.isPresent());
        // make sure both original and regenerated authenticators have been cached
        final String newAuthenticator = session.get().getAuthenticator().get().toString();
        assertNotSame("Authenticator should have been reset.", authenticator, newAuthenticator);
        assertEquals("Unexpected number of session in cache.", 2, coSession.getCache().size());
        assertNotNull("Original authenticator should be present in cache.", coSession.getCache().getIfPresent(authenticator));
        assertNotNull("New authenticator should be present in cache.", coSession.getCache().getIfPresent(newAuthenticator));
        assertSame("Original and new authenticators should point to the same session instance.", coSession.getCache().getIfPresent(authenticator), coSession.getCache().getIfPresent(newAuthenticator));

        // more time passes, which should get authenticators evicted from cache
        // please note, that because both the original and regenerated authenticator get placed into the cache at the same time
        // their eviction would also happen simultaneously
        constants.setNow(dateTime("2015-04-23 13:16:00"));

        // making a request with the original authenticator should lead to blocking of user sessions due to suspected stolen authenticator
        final Optional<UserSession> ss = coSession.currentSession(currUser, authenticator);
        assertFalse(ss.isPresent());
        final EntityResultQueryModel<UserSession> currUserSessions = select(UserSession.class).where().prop("user").eq().val(currUser).model();
        assertEquals(0, coSession.count(currUserSessions));
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