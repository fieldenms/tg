package ua.com.fielden.platform.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.security.SignatureException;
import java.util.Optional;

import org.junit.Test;

import com.google.common.base.Ticker;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
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
 * a different than stolen device, followed by an attempt to be used by a legitimate user.
 *
 * @author TG Team
 *
 */
public class UserSessionRecognisingStolenAndResuedAuthenticatorTestCase extends AbstractDaoTestCase {

    private final IUserSession coSession = ao(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final TickerForSessionCache cacheTicker = (TickerForSessionCache) getInstance(Ticker.class);

    @Test
    public void should_recognise_situation_with_stolen_and_used_authenticator_upon_a_legitimate_attempt_to_use_that_authenticator_by_valid_user() throws SignatureException {
        // establish a new sessions for user TEST
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("TEST", getInstance(IUser.class));
        final User currUser = getInstance(IUserProvider.class).getUser();

        constants.setNow(dateTime("2015-04-23 13:00:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 13:00:00"));
        final UserSession newSession = coSession.newSession(currUser, true);

        // authenticator that is associated with just established session gets stolen by an adversary....
        final String stolenAuthenticator = newSession.getAuthenticator().get().toString();

        // some time passes by....enough to evict the cached session
        constants.setNow(dateTime("2015-04-23 14:00:00"));

        // our adversary attempts to use the stolen authenticator, which leads to a session refresh, basically invalidating the stolen authenticator...
        final Optional<UserSession> adversarySession = coSession.currentSession(currUser, stolenAuthenticator);
        assertTrue("Aversary should have successfully accessed the system", adversarySession.isPresent());
        // let's capture authenticator that will be used by the adversary during the next attempt to access the system
        final String adversaryAuthenticator = adversarySession.get().getAuthenticator().get().toString();
        assertNotEquals("Authenticators should have been different due to expected series id regenration", stolenAuthenticator, adversaryAuthenticator);

        // the stolen authenticator would not get refreshed on the user's trusted device where it was originated...
        // therefore, when more time passes by, and our legitimate user tries to access the system from that trusted device for the first time since the the authenticator was stolen...
        // s/he is up for a surprise -- the session is not recognised as valid, and requests explicit login!
        constants.setNow(dateTime("2015-04-23 14:05:00"));

        final Optional<UserSession> userSession = coSession.currentSession(currUser, stolenAuthenticator);
        assertFalse("User should have been denied access.", userSession.isPresent());

        // at this stage all sessions for the compromised user shoud have been removed, but not the sessions for other users
        final EntityResultQueryModel<UserSession> currUserSessions = select(UserSession.class).where().prop("user").eq().val(currUser).model();
        final EntityResultQueryModel<UserSession> otherSessions = select(UserSession.class).where().prop("user").ne().val(currUser).model();

        assertEquals(0, coSession.count(currUserSessions));
        assertEquals(2, coSession.count(otherSessions));

        // not only the compromised user is up for a surprise with the requirement to login again -- the adversary too!!!
        // but the adversary does not know this user password, which prevents all further access to the system for that adversary!!!
        final Optional<UserSession> newAdversarySession = coSession.currentSession(currUser, adversaryAuthenticator);
        assertFalse("Adversary should have been denied access post user's attempt to access it.", newAdversarySession.isPresent());


    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        // add more users
        final IUser coUser = ao(User.class);
        save(new_(TgPerson.class, "Person 1").setUser(coUser.save(new_(User.class, "USER-1").setBase(true))));
        save(new_(TgPerson.class, "Person 2").setUser(coUser.save(new_(User.class, "USER-2").setBase(true))));

        // establish session for the above users
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("USER-1", getInstance(IUser.class));
        final User user1 = up.getUser();

        // trusted session for User-1
        constants.setNow(dateTime("2015-04-23 16:26:00"));
        cacheTicker.setStartTime(dateTime("2015-04-23 16:26:00"));

        coSession.newSession(user1, true); // from work

        up.setUsername("USER-2", getInstance(IUser.class));
        final User user2 = up.getUser();

        // trusted session for User-2
        constants.setNow(dateTime("2015-04-23 16:30:00"));
        coSession.newSession(user2, true); // from work
    }

}