package ua.com.fielden.platform.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.security.SignatureException;
import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case to cover cases of fraudulent user session authentication attempts.
 *
 * @author TG Team
 *
 */
public class UserSessionFraudulentAuthenticationAttemptsTestCase extends AbstractDaoTestCase {

    private final IUserSession coSession = ao(UserSession.class);
    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);

    @Test
    public void should_recognize_if_adversary_obtained_authenticator_from_untrusted_device_and_tried_to_change_expiry_date() throws SignatureException {
        // establish a new sessions for user TEST
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("TEST", getInstance(IUserEx.class));
        final User currUser = getInstance(IUserProvider.class).getUser();

        // first session is from trusted device
        constants.setNow(dateTime("2015-04-23 11:00:00"));
        coSession.newSession(currUser, true);

        // second session from untrusted device, and this is the device where the authenticator gets stolen from
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        final UserSession newSession = coSession.newSession(currUser, false);

        // let's fabricate a fraudulent authenticator
        final String fraudulentAuthenticator = newSession.getAuthenticator().get().username + Authenticator.AUTHENTICATOR_SEPARATOR +
                                               newSession.getAuthenticator().get().seriesId + Authenticator.AUTHENTICATOR_SEPARATOR +
                                               // tries to move expiration time into the future
                                               (constants.now().plusMinutes(15).getMillis()) + Authenticator.AUTHENTICATOR_SEPARATOR +
                                               newSession.getAuthenticator().get().hash;


        // now let's move the clock 7 minutes forward to emulate a time change and request a current session with fraudulent authenticator
        // that as far as the adversary is concerned should still be valid due to the performed time manipulation
        constants.setNow(dateTime("2015-04-23 13:07:00"));
        final Optional<UserSession> session = coSession.currentSession(currUser, fraudulentAuthenticator);
        assertFalse(session.isPresent());

        // additionally, let's also check that all sessions for a compromised user have been removed, but not the sessions for other users
        final EntityResultQueryModel<UserSession> currUserSessions = select(UserSession.class).where().prop("user").eq().val(currUser).model();
        final EntityResultQueryModel<UserSession> otherSessions = select(UserSession.class).where().prop("user").ne().val(currUser).model();

        assertEquals(0, coSession.count(currUserSessions));
        assertEquals(2, coSession.count(otherSessions));
    }


    @Test
    public void should_recognize_if_adversary_obtained_authenticator_from_trusted_device_and_tried_connect_under_a_different_user() throws SignatureException {
        // establish a new sessions for user TEST
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("TEST", getInstance(IUserEx.class));
        final User currUser = getInstance(IUserProvider.class).getUser();

        // first session is from trusted device
        constants.setNow(dateTime("2015-04-23 11:00:00"));
        coSession.newSession(currUser, true);

        // second session, also from a trusted device, the authenticator from this device got stolen
        constants.setNow(dateTime("2015-04-23 13:00:00"));
        final UserSession newSession = coSession.newSession(currUser, true);
        final String authenticator = newSession.getAuthenticator().get().toString();


        // now let's move the clock 30 minutes forward to emulate a time change
        // adversary tries to reuse a completely valid and not yet expired authenticator to access the system under a different username
        constants.setNow(dateTime("2015-04-23 13:30:00"));
        up.setUsername("USER-1", getInstance(IUserEx.class));
        final User differentUser = getInstance(IUserProvider.class).getUser();

        final Optional<UserSession> session = coSession.currentSession(differentUser, authenticator);
        assertFalse(session.isPresent());

        // additionally, let's also check that all sessions for a compromised user have been removed, but not the sessions for other users
        final EntityResultQueryModel<UserSession> currUserSessions = select(UserSession.class).where().prop("user").eq().val(currUser).model();
        final EntityResultQueryModel<UserSession> otherSessions = select(UserSession.class).where().prop("user").ne().val(currUser).model();

        assertEquals(0, coSession.count(currUserSessions));
        assertEquals(2, coSession.count(otherSessions));
    }


    @Override
    protected void populateDomain() {
        super.populateDomain();

        // add more users
        save(new_(TgPerson.class, "Person 1").setUsername("USER-1").setBase(true));
        save(new_(TgPerson.class, "Person 2").setUsername("USER-2").setBase(true));

        // establish session for the above users
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("USER-1", getInstance(IUserEx.class));
        final User user1 = up.getUser();

        // trusted session for User-1
        constants.setNow(dateTime("2015-04-23 16:26:00"));
        coSession.newSession(user1, true); // from work

        up.setUsername("USER-2", getInstance(IUserEx.class));
        final User user2 = up.getUser();

        // trusted session for User-2
        constants.setNow(dateTime("2015-04-23 16:30:00"));
        coSession.newSession(user2, true); // from work
    }

}