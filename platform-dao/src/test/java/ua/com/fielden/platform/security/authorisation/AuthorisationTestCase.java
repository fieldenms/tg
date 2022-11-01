package ua.com.fielden.platform.security.authorisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.devdb_support.SecurityTokenAssociator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.security_tokens.DeleteFuelTypeToken;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class AuthorisationTestCase extends AbstractDaoTestCase {
    private static final String fuelType = "U";
    private final String permissiveUsername = "TESTUSER";
    private final String restrictiveUsername = "TESTUSERRESTRICTIVE";
    private final String roleName = "ADMINISTRATION";

    @Test
    public void permissive_user_should_be_able_to_delete_fuel_types() {
        final TgFuelType ft = co$(TgFuelType.class).findByKey(fuelType);
        assertNotNull(ft);

        co$(TgFuelType.class).delete(ft);

        assertNull(co$(TgFuelType.class).findByKey(fuelType));
    }

    @Test
    public void restrictive_user_should_not_be_able_to_delete_fuel_types() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(restrictiveUsername, getInstance(IUser.class));

        final TgFuelType ft = co$(TgFuelType.class).findByKey(fuelType);
        assertNotNull(ft);
        try {
            co$(TgFuelType.class).delete(ft);
            fail();
        } catch (final Result ex) {
            assertEquals("Permission denied due to token [Delete Fuel Type] restriction.", ex.getMessage());
        }
    }

    @Test
    public void originally_permissive_user_becomes_restrictive_once_its_role_gets_deactivated() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(UNIT_TEST_USER, co$(User.class));

        final UserRole role = co$(UserRole.class).findByKey(roleName);
        assertTrue(role.isActive());
        // let's now deactivate the role
        save(role.setActive(false));


        // switch back to a permissive user and try to performing a "permissive" action
        up.setUsername(permissiveUsername, co$(User.class));
        final TgFuelType ft = co$(TgFuelType.class).findByKey(fuelType);
        assertNotNull(ft);

        try {
            co$(TgFuelType.class).delete(ft);
            fail();
        } catch (final Result ex) {
            assertEquals("Permission denied due to token [Delete Fuel Type] restriction.", ex.getMessage());
        }
    }

    @Before
    public void setUp() {
        // set permissive user as the current user before each test
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(permissiveUsername, getInstance(IUser.class));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        // for testing authorisation we need a user, a role and association between that user and role, and role with designated security token
        // so, create persons that are users at the same time -- one permissive and one restrictive
        final IUser coUser = co$(User.class);
        save(new_(TgPerson.class, "Permissive Person").setUser(coUser.save(new_(User.class, permissiveUsername).setBase(true))));
        save(new_(TgPerson.class, "Restrictive Person").setUser(coUser.save(new_(User.class, restrictiveUsername).setBase(true))));


        // now create a user role

        final UserRole adminRole = save(new_(UserRole.class, roleName, "A role, which has a full access to the the system and should be used only for users who need administrative previligies.").setActive(true));
        // ... and associate it with our user
        final User testUser = co$(User.class).findByKey(permissiveUsername);
        save(new_composite(UserAndRoleAssociation.class, testUser, adminRole));

        // now let's reuse our standard logic for associating roles and security tokens, which is the last step in this security setup process
        // please note that in this test case only a top level security token is used
        // in case of sub tokens, a tree -- not just a single node would need to be created
        final SecurityTokenNode node = new SecurityTokenNode(DeleteFuelTypeToken.class.getName(), "Delete Fuel Type", "Controls deletion of fuel types.");
        final SecurityTokenAssociator predicate = new SecurityTokenAssociator(adminRole, co$(SecurityRoleAssociation.class));
        predicate.eval(node);

        // we also need some fuel types, so that we could test whether their deletion is guarded by token
        save(new_(TgFuelType.class, fuelType, "Unleaded"));
    }

}