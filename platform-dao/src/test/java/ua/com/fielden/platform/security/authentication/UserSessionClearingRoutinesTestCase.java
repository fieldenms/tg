package ua.com.fielden.platform.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.security.SignatureException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.TickerForSessionCache;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case to cover various clearing routines of user session.
 *
 * @author TG Team
 *
 */
public class UserSessionClearingRoutinesTestCase extends AbstractDaoTestCase {

    private final UserSessionDao coSession = (UserSessionDao) co$(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final Cache<String, UserSession> cache = coSession.getCache();
    private final TickerForSessionCache cacheTicker = (TickerForSessionCache) getInstance(Ticker.class);

    @Before
    public void startUp() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(UNIT_TEST_USER, co(User.class));
        // due to global cache nature it needs to be invalidated in order to keep tests independent
        cache.invalidateAll();
    }


    @Test
    public void only_expired_user_session_should_have_been_removed() throws SignatureException {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(UNIT_TEST_USER, getInstance(IUser.class));
        final User currUser = getInstance(IUserProvider.class).getUser();
        // let's try to clear all expired sessions and check if they're indeed cleared
        constants.setNow(dateTime("2015-04-24 07:31:00"));
        coSession.clearExpired(currUser);

        final EntityResultQueryModel<UserSession> query = select(UserSession.class).where().prop("user").eq().val(currUser).model();
        final QueryExecutionModel<UserSession, EntityResultQueryModel<UserSession>> qem = from(query).with(fetchAll(UserSession.class)).with(orderBy().prop("lastAccess").asc().model()).model();

        final List<UserSession> remainingSessions = coSession.getAllEntities(qem);
        assertEquals(3, remainingSessions.size());
        assertTrue(remainingSessions.get(0).isTrusted());
        assertTrue(remainingSessions.get(1).isTrusted());
        assertFalse(remainingSessions.get(2).isTrusted());
    }

    @Test
    public void all_user_sessions_should_have_been_removed() throws SignatureException {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(UNIT_TEST_USER, getInstance(IUser.class));
        final User currUser = getInstance(IUserProvider.class).getUser();

        coSession.clearAll(currUser);

        assertEquals(0, coSession.count(select(UserSession.class).where().prop("user").eq().val(currUser).model()));
        assertEquals(3, coSession.count(select(UserSession.class).where().prop("user").ne().val(currUser).model()));
    }

    @Test
    public void all_user_untrusted_sessions_should_have_been_removed() throws SignatureException {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(UNIT_TEST_USER, getInstance(IUser.class));
        final User currUser = getInstance(IUserProvider.class).getUser();

        constants.setNow(dateTime("2015-04-24 07:31:00"));
        coSession.clearUntrusted(currUser);

        final EntityResultQueryModel<UserSession> query = select(UserSession.class).where().prop("user").eq().val(currUser).model();
        final QueryExecutionModel<UserSession, EntityResultQueryModel<UserSession>> qem = from(query).with(fetchAll(UserSession.class)).model();

        final List<UserSession> remainingSessions = coSession.getAllEntities(qem);
        assertEquals(2, remainingSessions.size());
        remainingSessions.forEach(us -> assertTrue(us.isTrusted()));
    }


    @Test
    public void only_expired_sessions_but_for_all_users_should_have_been_removed() throws SignatureException {
        // let's try to clear all expired sessions and check if they're indeed cleared
        constants.setNow(dateTime("2015-04-24 07:34:00"));
        coSession.clearExpired();

        final EntityResultQueryModel<UserSession> query = select(UserSession.class).model();
        final QueryExecutionModel<UserSession, EntityResultQueryModel<UserSession>> qem = from(query).with(fetchAll(UserSession.class)).with(orderBy().prop("id").asc().model()).model();

        final List<UserSession> remainingSessions = coSession.getAllEntities(qem);
        assertEquals(5, remainingSessions.size());
        assertTrue(remainingSessions.get(0).isTrusted());
        assertTrue(remainingSessions.get(1).isTrusted());
        assertFalse(remainingSessions.get(2).isTrusted());
        assertTrue(remainingSessions.get(3).isTrusted());
        assertFalse(remainingSessions.get(4).isTrusted());
    }

    @Test
    public void all_untrusted_sessions_for_all_users_should_have_been_removed() throws SignatureException {
        constants.setNow(dateTime("2015-04-24 07:34:00"));
        coSession.clearUntrusted();

        assertEquals(3, coSession.count(select(UserSession.class).model()));
    }


    @Test
    public void all_sessions_for_all_users_should_have_been_removed() throws SignatureException {
        coSession.clearAll();
        assertEquals(0, coSession.count(select(UserSession.class).model()));
    }

    @Test
    public void clearing_user_sessions_by_sid_removes_associated_sessions_from_cache_and_database() {
        // establish a new sessions for user TEST, which effectively emulates the explicit login
        final String sidForUser1 = "5daf08eb-9dcd-4baa-91e3-51d3daed5ba5";
        getInstance(IUserProvider.class).setUsername("USER1", co(User.class));
        final User currUser1 = getInstance(IUserProvider.class).getUser();
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 13:00:00"));
        final UserSession newSessionForUser1 = coSession.newSession(currUser1, true, sidForUser1);
        final String authenticator = newSessionForUser1.getAuthenticator().get().toString();

        // enough time has passed to evict authenticators from cache
        constants.setNow(dateTime("2015-04-23 13:06:00"));

        // try to get the current session using the evicted, but valid authenticator
        // this should lead to creation of a new authenticator and registration of a new session
        // that would be associated with the presented and new authenticators in cache
        final Optional<UserSession> renewdSessionForUser1 = coSession.currentSession(currUser1, authenticator, false);
        assertTrue(renewdSessionForUser1.isPresent());
        final String newAuthenticator = renewdSessionForUser1.get().getAuthenticator().get().toString();
        assertNotEquals("The new and subsequent session should have different IDs.", newSessionForUser1.getId(), renewdSessionForUser1.get().getId());
        assertEquals("A sid value should persiste between sessions.", sidForUser1, renewdSessionForUser1.get().getSid());
        assertEquals("Unexpected number of session in cache.", 2, coSession.getCache().size());
        
        // let's now create a session for some other user
        getInstance(IUserProvider.class).setUsername("USER2", co(User.class));
        final User currUser2 = getInstance(IUserProvider.class).getUser();
        final UserSession newSessionForUser2 = coSession.newSession(currUser2, true, "gda108eb-9dcd-4baa-18d3-51d3daed5ba5");
        assertEquals("Unexpected number of session in cache.", 3, coSession.getCache().size());
        
        // and clear sessions by sid for USER1, which should remove 2 records from the database and clear the cache accordingly
        assertEquals(2, coSession.clearAllWithSid(sidForUser1));
        assertEquals(1, coSession.getCache().size());

        // more time has passed and all sessions should have been evicted
        constants.setNow(dateTime("2015-04-23 13:15:00"));
        coSession.getCache().cleanUp(); // force eviction of expired sessions
        assertEquals(0, coSession.getCache().size());
        // no current session for User 1 is expected due to session clearing
        final Optional<UserSession> renewdSessionForUser1PostClearing = coSession.currentSession(currUser1, newAuthenticator, false);
        assertFalse("There should be no current session for USER1.", renewdSessionForUser1PostClearing.isPresent());
        // however, User 2 should be able to renew their session
        final Optional<UserSession> renewdSessionForUser2 = coSession.currentSession(currUser2, newSessionForUser2.getAuthenticator().get().toString(), false);
        assertTrue("There should be the current session for USER2.", renewdSessionForUser2.isPresent());
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();

        constants.setNow(dateTime("2015-04-23 15:00:00"));

        // add more users
        final IUser coUser = co$(User.class);
        final User user1 = coUser.save(new_(User.class, "USER1").setBase(true).setEmail("USER1@unit-test.software").setActive(true));
        save(new_(TgPerson.class, "Person 1").setUser(user1));
        final User user2 = coUser.save(new_(User.class, "USER2").setBase(true).setEmail("USER2@unit-test.software").setActive(true));
        save(new_(TgPerson.class, "Person 2").setUser(user2));

        final User currUser = getInstance(IUserProvider.class).getUser();
        // establish several trusted sessions at different times
        constants.setNow(dateTime("2015-04-23 16:26:00"));
        coSession.newSession(currUser, true, null); // from work
        constants.setNow(dateTime("2015-04-23 18:26:00"));
        coSession.newSession(currUser, true, null); // from home

        // establish several untrusted sessions
        constants.setNow(dateTime("2015-04-23 19:30:00"));
        coSession.newSession(currUser, false, null);
        constants.setNow(dateTime("2015-04-23 20:30:00"));
        coSession.newSession(currUser, false, null);
        constants.setNow(dateTime("2015-04-24 07:30:00"));
        coSession.newSession(currUser, false, null);

        // set some sessions for USER1
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("USER1", getInstance(IUser.class));

        // trusted session for USER1
        constants.setNow(dateTime("2015-04-23 16:26:00"));
        coSession.newSession(user1, true, null); // from work

        // untrusted sessions for USER1
        constants.setNow(dateTime("2015-04-23 19:30:00"));
        coSession.newSession(user1, false, null);
        constants.setNow(dateTime("2015-04-24 07:32:00"));
        coSession.newSession(user1, false, null);
    }
}