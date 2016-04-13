package ua.com.fielden.web.security.userroleaccosication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.SecurityTokenControllerRao;
import ua.com.fielden.platform.security.UserControllerRao;
import ua.com.fielden.platform.security.UserRoleRao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.factories.SecurityTokenResourceFactory;
import ua.com.fielden.platform.web.factories.TokenRoleAssociationResourceFactory;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;

/**
 * Provides a unit test for security token/role management and user authorisation.
 * 
 * @author TG Team
 * 
 */
public class SecurityTokenManagementTestCase extends WebBasedTestCase {

    private final IUserRoleDao userRoleRao = new UserRoleRao(config.restClientUtil());
    private final IUser coUser = new UserControllerRao(userRoleRao, config.restClientUtil());
    private final ISecurityTokenController tokenControllerRao = new SecurityTokenControllerRao(userRoleRao, config.restClientUtil());

    @Test
    public void test_that_all_user_roles_can_be_found() {
        final List<? extends UserRole> roles = tokenControllerRao.findUserRoles();
        assertEquals("Incorrect number of roles.", 8, roles.size());
    }

    @Test
    public void test_can_find_roles_for_token() {
        final List<? extends UserRole> roles = tokenControllerRao.findUserRolesFor(FirstLevelSecurityToken1.class);
        assertEquals("Incorrect number of roles for token.", 2, roles.size());
    }

    @SuppressWarnings("serial")
    @Test
    public void test_can_save_changes_to_token_role_association() {
        final List<? extends UserRole> roles = new ArrayList<>(tokenControllerRao.findUserRolesFor(FirstLevelSecurityToken1.class));
        assertEquals("Incorrect number of roles for token.", 2, roles.size());

        final Map<Class<? extends ISecurityToken>, Set<UserRole>> newAssociations = new HashMap<Class<? extends ISecurityToken>, Set<UserRole>>();
        newAssociations.put(FirstLevelSecurityToken1.class, new HashSet<UserRole>() {
            {
                add(roles.get(0));
            }
        });
        tokenControllerRao.saveSecurityToken(newAssociations);

        assertEquals("Incorrect number of roles for token after update.", 1, tokenControllerRao.findUserRolesFor(FirstLevelSecurityToken1.class).size());
    }

    @Test
    public void test_that_retreive_all_association_works() {
        final Map<Class<? extends ISecurityToken>, Set<UserRole>> associations = tokenControllerRao.findAllAssociations();
        assertEquals("Incorrect number of token roles aasociation.", 6, associations.size());

        for (final Map.Entry<Class<? extends ISecurityToken>, Set<UserRole>> associationEntry : associations.entrySet()) {
            assertEquals("Incorrect number of roles aasociated with " + associationEntry.getKey().getSimpleName(), 2, associationEntry.getValue().size());
        }
    }

    @Test
    public void test_count_association_between_user_and_token() {
        final String userName = config.restClientUtil().getUsername();
        config.restClientUtil().setUsername("USER-1");
        final User user = coUser.findByKey("USER-1");
        assertTrue("Access should be authorised.", tokenControllerRao.canAccess(user, FirstLevelSecurityToken1.class));
        assertTrue("Access should be authorised.", tokenControllerRao.canAccess(user, ThirdLevelSecurityToken1.class));
        assertFalse("Access should not be authorised.", tokenControllerRao.canAccess(user, ThirdLevelSecurityToken2.class));
        config.restClientUtil().setUsername(userName);
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());

        final RouterHelper helper = new RouterHelper(DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
        helper.register(router, IUserRoleDao.class);
        helper.register(router, IUser.class);

        final Restlet securityTokenRestlet = new SecurityTokenResourceFactory(DbDrivenTestCase.injector);
        router.attach("/users/{username}/securitytokens", securityTokenRestlet);
        router.attach("/users/{username}/securitytokens/{token}", securityTokenRestlet); // authorisation resources
        router.attach("/users/{username}/securitytokens/{token}/useroles", securityTokenRestlet);
        final Restlet tokenRoleAssociationRestlet = new TokenRoleAssociationResourceFactory(DbDrivenTestCase.injector);
        router.attach("/users/{username}/tokenroleassociation", tokenRoleAssociationRestlet);

        return router;
    }

    @Override
    protected String[] getDataSetPaths() {
        return new String[] { "src/test/resources/data-files/user-role-test-case.flat.xml" };
    }

}
