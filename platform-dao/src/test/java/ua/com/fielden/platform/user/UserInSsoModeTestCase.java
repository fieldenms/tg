package ua.com.fielden.platform.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.test.runners.H2TgDomainDrivenTestCaseInSsoAuthModeRunner;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * A test case to cover user instantiation in the SSO authentication mode.
 * <p>
 * The use of {@link RunWith} with an alternative runner {@link H2TgDomainDrivenTestCaseInSsoAuthModeRunner} is required for this test case.
 * This runner overrides configuration properties to set the SSO authentication mode.
 * However, it is only possible to run this test case individually due to caching of configurations statically in the base runner class.
 * This is the reason why the runner annotation is commented out and tests are annotated with {@link Ignore}.
 * <p>
 * At some stage we need to consider a way to support multiple runner implementations without taking a significant performance hit.
 *
 * @author TG Team
 *
 */
//@RunWith(H2TgDomainDrivenTestCaseInSsoAuthModeRunner.class)
public class UserInSsoModeTestCase extends AbstractDaoTestCase {

    @Test
    @Ignore
    public void in_SSO_authentication_mode_new_users_have_property_ssoOnly_equal_true_and_editable() {
        assertEquals("Invalid initial conditions for this test.", AuthMode.SSO, getInstance(IApplicationSettings.class).authMode());

        final User user = co$(User.class).new_();
        assertTrue(user.isSsoOnly());
        assertTrue(user.getProperty(User.SSO_ONLY).isEditable());
    }

    @Test
    @Ignore
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
    @Ignore
    public void in_SSO_authentication_mode_only_users_not_restricted_to_SSO_only_can_have_password_reset_UUID_generated() {
        final IUser coUser = co(User.class);
        final Optional<UserSecret> secretForNotRestrictedUser = coUser.assignPasswordResetUuid("USER4");
        assertTrue(secretForNotRestrictedUser.isPresent());
        assertNotNull(secretForNotRestrictedUser.get().getResetUuid());

        final Optional<UserSecret> secretForRestrictedUser = coUser.assignPasswordResetUuid("USER3");
        assertFalse(secretForRestrictedUser.isPresent());
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();

        co$(User.class).save(new_(User.class, "USER3").setBase(true).setEmail("USER3@company.com").setActive(true));
        co$(User.class).save(new_(User.class, "USER4").setBase(true).setEmail("USER4@company.com").setActive(true).setSsoOnly(false));
    }

}