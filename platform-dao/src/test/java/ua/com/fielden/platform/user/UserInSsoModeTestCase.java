package ua.com.fielden.platform.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.security.user.UserSecretCo;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.test_config.SsoAuthModeContextSelector;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.security.user.UserSecretCo.RESET_UUID_EXPIRATION_IN_MINUTES;

/// A test case to cover users in the SSO authentication mode.
///
/// [SsoAuthModeContextSelector] runs this test case with a test configuration in the SSO authentication mode, which is separate from the configuration of other test cases.
///
@RunWith(SsoAuthModeContextSelector.class)
public class UserInSsoModeTestCase extends AbstractDaoTestCase {

    @Test
    public void in_SSO_authentication_mode_new_users_have_property_ssoOnly_equal_true_and_editable() {
        assertEquals("Invalid initial conditions for this test.", AuthMode.SSO, getInstance(IApplicationSettings.class).authMode());

        final User user = co$(User.class).new_();
        assertTrue(user.isSsoOnly());
        assertTrue(user.getProperty(User.SSO_ONLY).isEditable());
    }

    @Test
    public void in_SSO_authentication_mode_retrieved_for_editing_users_have_property_ssoOnly_equal_true_and_editable() {
        assertEquals("Invalid initial conditions for this test.", AuthMode.SSO, getInstance(IApplicationSettings.class).authMode());

        final User userWithSsoOnly = co$(User.class).findByKey("USER3");
        assertTrue(userWithSsoOnly.isSsoOnly());
        assertTrue(userWithSsoOnly.getProperty(User.SSO_ONLY).isEditable());

        final User userWithRsoPermission = co$(User.class).findByKey("USER4");
        assertFalse(userWithRsoPermission.isSsoOnly());
        assertTrue(userWithRsoPermission.getProperty(User.SSO_ONLY).isEditable());
    }

    @Test
    public void in_SSO_authentication_mode_only_users_not_restricted_to_SSO_only_can_have_password_reset_UUID_generated() {
        final IUser coUser = co(User.class);
        final Optional<UserSecret> secretForNotRestrictedUser = coUser.assignPasswordResetUuid("USER4", expirationTime());
        assertTrue(secretForNotRestrictedUser.isPresent());
        assertNotNull(secretForNotRestrictedUser.get().getResetUuid());

        final Optional<UserSecret> secretForRestrictedUser = coUser.assignPasswordResetUuid("USER3", expirationTime());
        assertFalse(secretForRestrictedUser.isPresent());
    }

    @Test
    public void in_SSO_authentication_mode_only_users_not_restricted_to_SSO_only_can_be_found_for_password_login() {
        assertEquals("Invalid initial conditions for this test.", AuthMode.SSO, getInstance(IApplicationSettings.class).authMode());

        final IUser coUser = co(User.class);
        assertTrue(coUser.findUserForPasswordLogin("USER4").isPresent());
        assertTrue(coUser.findUserForPasswordLogin("USER3").isEmpty());
    }

    @Test
    public void in_SSO_authentication_mode_lockout_deactivates_only_users_not_restricted_to_SSO_only() {
        assertEquals("Invalid initial conditions for this test.", AuthMode.SSO, getInstance(IApplicationSettings.class).authMode());

        final IUser coUser = co(User.class);
        final UserSecretCo coUserSecret = co(UserSecret.class);
        // A user restricted to SSO only has no secret, unless a password was set for them.
        coUser.resetPasswd(coUser.findUser("USER3"), "a strong password for USER3");
        coUser.resetPasswd(coUser.findUser("USER4"), "a strong password for USER4");
        coUser.lockoutUser("USER3");
        coUser.lockoutUser("USER4");

        final User userWithSsoOnly = coUser.findUser("USER3");
        assertTrue(userWithSsoOnly.isActive());
        assertTrue(coUserSecret.findByIdOptional(userWithSsoOnly.getId()).isPresent());
        final User userWithPassword = coUser.findUser("USER4");
        assertFalse(userWithPassword.isActive());
        assertTrue(coUserSecret.findByIdOptional(userWithPassword.getId()).isEmpty());
    }

    private Date expirationTime() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        return consts.now().plusMinutes(RESET_UUID_EXPIRATION_IN_MINUTES).toDate();
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        co$(User.class).save(new_(User.class, "USER3").setBase(true).setEmail("USER3@company.com").setActive(true));
        co$(User.class).save(new_(User.class, "USER4").setBase(true).setEmail("USER4@company.com").setActive(true).setSsoOnly(false));
    }

}