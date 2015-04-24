package ua.com.fielden.platform.security.authorisation;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.devdb_support.SecurityTokenAssociator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.security_tokens.DeleteFuelTypeToken;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

public class AuthorisationTestCase extends AbstractDomainDrivenTestCase {
    private static final String fuelType = "U";
    private final String permissiveUsername = "TEST-USER";
    private final String restrictiveUsername = "TEST-USER-RESTRICTIVE";

    @Test
    public void permissive_user_should_be_able_to_delete_fuel_types() {
        final TgFuelType ft = ao(TgFuelType.class).findByKey(fuelType);
        assertNotNull(ft);

        ao(TgFuelType.class).delete(ft);

        assertNull(ao(TgFuelType.class).findByKey(fuelType));
    }

    @Test
    public void restrictive_user_should_not_be_able_to_delete_fuel_types() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(restrictiveUsername, getInstance(IUserController.class));

        final TgFuelType ft = ao(TgFuelType.class).findByKey(fuelType);
        assertNotNull(ft);
        try {
            ao(TgFuelType.class).delete(ft);
            fail();
        } catch (final Result ex) {
            assertEquals("Permission denied due to token Delete Fuel Type restriction.", ex.getMessage());
        }

    }

    @Before
    public void setUp() {
        // set permissive user as the current user before each test
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(permissiveUsername, getInstance(IUserController.class));
    }

    @Override
    protected void populateDomain() {
        // for testing authorisation we need a user, a role and association between that user and role, and role with designated security token
        // so, create persons that are users at the same time -- one permissive and one restrictive
        save(new_(TgPerson.class, "Permissive Person").setUsername(permissiveUsername).setBase(true));
        save(new_(TgPerson.class, "Restrictive Person").setUsername(restrictiveUsername).setBase(true));

        // now create a user role
        final UserRole adminRole = save(new_(UserRole.class, "ADMINISTRATION", "A role, which has a full access to the the system and should be used only for users who need administrative previligies."));
        // ... and associate it with our user
        final User testUser = ao(User.class).findByKey(permissiveUsername);
        save(new_composite(UserAndRoleAssociation.class, testUser, adminRole));

        // now let's reuse our standard logic for associating roles and security tokens, which is the last step in this security setup process
        // please note that in this test case only a top level security token is used
        // in case of sub tokens, a tree -- not just a single node would need to be created
        final SecurityTokenNode node = SecurityTokenNode.makeTopLevelNode(DeleteFuelTypeToken.class);
        final SecurityTokenAssociator predicate = new SecurityTokenAssociator(adminRole, ao(SecurityRoleAssociation.class));
        predicate.eval(node);

        // we also need some fuel types, so that we could test whether their deletion is guarded by token
        save(new_(TgFuelType.class, fuelType, "Unleaded"));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }
}