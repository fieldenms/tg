package ua.com.fielden.platform.web.resources.webui;

import org.junit.Test;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.security.user.UserSecretCo;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_BY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;

/// Tests for the lockout of an account by [AccountLockout] on behalf of a login attempt, which has no current user.
///
public class AccountLockoutTest extends AbstractWebResourceWithDaoTestCase {

    @Test
    public void lockout_is_performed_asynchronously_and_deactivates_the_user_and_removes_their_secret_with_SU_as_the_current_user() {
        final IUser coUser = co$(User.class);
        final UserSecretCo coUserSecret = co(UserSecret.class);
        final User user = coUser.findUser("USER1");
        assertTrue("Invalid initial conditions for this test.", coUserSecret.findByIdOptional(user.getId()).isPresent());

        // The executor queues the lockout, which is performed explicitly below.
        final List<Runnable> queue = new ArrayList<>();
        final IUserProvider up = getInstance(IUserProvider.class);
        final AccountLockout accountLockout = new AccountLockout(getInstance(ICompanionObjectFinder.class), up, queue::add);

        final User currUser = up.getUser();
        up.clearUser();
        try {
            accountLockout.lockoutAsync("USER1");
            assertTrue("The lockout should not be performed by the requesting thread.", coUser.findUser("USER1").isActive());
            assertEquals(1, queue.size());

            queue.getFirst().run();
            assertNull("The current user should be cleared after lockout.", up.getUser());
        } finally {
            up.setUser(currUser);
        }

        final User lockedOutUser = coUser.findByKeyAndFetch(fetchKeyAndDescOnly(User.class).with(ACTIVE).with(LAST_UPDATED_BY), "USER1");
        assertFalse(lockedOutUser.isActive());
        assertEquals(User.system_users.SU.name(), lockedOutUser.getLastUpdatedBy().getKey());
        assertFalse(coUserSecret.findByIdOptional(user.getId()).isPresent());
    }

    @Test
    public void lockout_requested_while_one_is_pending_for_the_same_username_is_not_queued_again_and_is_queued_once_the_pending_one_has_completed() {
        final List<Runnable> queue = new ArrayList<>();
        final IUserProvider up = getInstance(IUserProvider.class);
        final AccountLockout accountLockout = new AccountLockout(getInstance(ICompanionObjectFinder.class), up, queue::add);

        accountLockout.lockoutAsync("USER1");
        accountLockout.lockoutAsync("USER1");
        accountLockout.lockoutAsync("user1");
        assertEquals("Lockouts requested for the same username (case-insensitive) while one is pending should not be queued again.", 1, queue.size());

        accountLockout.lockoutAsync("USER2");
        assertEquals("A lockout for another username should be queued.", 2, queue.size());

        final User currUser = up.getUser();
        try {
            queue.getFirst().run();
        } finally {
            up.setUser(currUser);
        }
        accountLockout.lockoutAsync("USER1");
        assertEquals("A lockout requested after the pending one has completed should be queued.", 3, queue.size());
        accountLockout.lockoutAsync("USER2");
        assertEquals(3, queue.size());
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
        coUser.save(new_(User.class, "USER2").setBase(true).setEmail("USER2@company.com").setActive(true));
    }

}
