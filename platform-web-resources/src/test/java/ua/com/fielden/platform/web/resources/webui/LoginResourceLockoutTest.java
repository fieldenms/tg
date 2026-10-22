package ua.com.fielden.platform.web.resources.webui;

import org.junit.Test;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.security.user.UserSecretCo;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_BY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.web.resources.webui.LoginResource.lockoutUser;

/// Tests for the lockout of an account, as performed by [LoginResource] on behalf of a login attempt, which has no current user.
///
public class LoginResourceLockoutTest extends AbstractWebResourceWithDaoTestCase {

    @Test
    public void lockout_on_behalf_of_a_login_attempt_deactivates_the_user_and_removes_their_secret_with_SU_as_the_current_user() {
        final IUser coUser = co$(User.class);
        final UserSecretCo coUserSecret = co(UserSecret.class);
        final User user = coUser.findUser("USER1");
        assertTrue("Invalid initial conditions for this test.", coUserSecret.findByIdOptional(user.getId()).isPresent());

        final IUserProvider up = getInstance(IUserProvider.class);
        final User currUser = up.getUser();
        up.clearUser();
        try {
            lockoutUser(up, coUser, "USER1");
            assertNull("The current user should be cleared after lockout.", up.getUser());
        } finally {
            up.setUser(currUser);
        }

        final User lockedOutUser = coUser.findByKeyAndFetch(fetchKeyAndDescOnly(User.class).with(ACTIVE).with(LAST_UPDATED_BY), "USER1");
        assertFalse(lockedOutUser.isActive());
        assertEquals(User.system_users.SU.name(), lockedOutUser.getLastUpdatedBy().getKey());
        assertFalse(coUserSecret.findByIdOptional(user.getId()).isPresent());
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final IUser coUser = co$(User.class);
        coUser.save(new_(User.class, User.system_users.SU.name()).setBase(true).setEmail("SU@company.com").setActive(true));
        coUser.save(new_(User.class, "USER1").setBase(true).setEmail("USER1@company.com").setActive(true));
    }

}
