package ua.com.fielden.platform.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.security.SignatureException;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
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

    private final IUserSession coSession = ao(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);

    @Test
    public void only_expired_user_session_should_have_been_removed() throws SignatureException {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("TEST", getInstance(IUser.class));
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
        up.setUsername("TEST", getInstance(IUser.class));
        final User currUser = getInstance(IUserProvider.class).getUser();

        coSession.clearAll(currUser);

        assertEquals(0, coSession.count(select(UserSession.class).where().prop("user").eq().val(currUser).model()));
        assertEquals(3, coSession.count(select(UserSession.class).where().prop("user").ne().val(currUser).model()));
    }

    @Test
    public void all_user_untrusted_sessions_should_have_been_removed() throws SignatureException {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("TEST", getInstance(IUser.class));
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

    @Override
    protected void populateDomain() {
        super.populateDomain();

        // add more users
        final IUser coUser = ao(User.class);
        final User user1 = coUser.save(new_(User.class, "USER1").setBase(true));
        save(new_(TgPerson.class, "Person 1").setUser(user1));
        final User user2 = coUser.save(new_(User.class, "USER2").setBase(true));
        save(new_(TgPerson.class, "Person 2").setUser(user2));

        final User currUser = getInstance(IUserProvider.class).getUser();
        // establish several trusted sessions at different times
        constants.setNow(dateTime("2015-04-23 16:26:00"));
        coSession.newSession(currUser, true); // from work
        constants.setNow(dateTime("2015-04-23 18:26:00"));
        coSession.newSession(currUser, true); // from home

        // establish several untrusted sessions
        constants.setNow(dateTime("2015-04-23 19:30:00"));
        coSession.newSession(currUser, false);
        constants.setNow(dateTime("2015-04-23 20:30:00"));
        coSession.newSession(currUser, false);
        constants.setNow(dateTime("2015-04-24 07:30:00"));
        coSession.newSession(currUser, false);

        // set some sessions for USER1
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("USER1", getInstance(IUser.class));

        // trusted session for USER1
        constants.setNow(dateTime("2015-04-23 16:26:00"));
        coSession.newSession(user1, true); // from work

        // untrusted sessions for USER1
        constants.setNow(dateTime("2015-04-23 19:30:00"));
        coSession.newSession(user1, false);
        constants.setNow(dateTime("2015-04-24 07:32:00"));
        coSession.newSession(user1, false);
    }
}