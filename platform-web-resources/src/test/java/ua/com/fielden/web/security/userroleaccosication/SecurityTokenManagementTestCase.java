package ua.com.fielden.web.security.userroleaccosication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.Router;

import ua.com.fielden.platform.dao2.IUserRoleDao2;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.SecurityTokenControllerRao;
import ua.com.fielden.platform.security.UserRoleRao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.DbDrivenTestCase2;
import ua.com.fielden.platform.web.SecurityTokenResourceFactory;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;

/**
 * Provides a unit test for security token/role management and user authorisation.
 *
 * @author TG Team
 *
 */
public class SecurityTokenManagementTestCase extends WebBasedTestCase {

    private final IUserRoleDao2 userRoleRao = new UserRoleRao(config.restClientUtil());
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

    @Test
    public void test_can_save_changes_to_token_role_association() {
	final List<? extends UserRole> roles = tokenControllerRao.findUserRolesFor(FirstLevelSecurityToken1.class);
	assertEquals("Incorrect number of roles for token.", 2, roles.size());

	final Map<Class<? extends ISecurityToken>, List<UserRole>> newAssociations = new HashMap<Class<? extends ISecurityToken>, List<UserRole>>();
	newAssociations.put(FirstLevelSecurityToken1.class, new ArrayList<UserRole>(){{add(roles.get(0));}});
	tokenControllerRao.saveSecurityToken(newAssociations);

	assertEquals("Incorrect number of roles for token after update.", 1, tokenControllerRao.findUserRolesFor(FirstLevelSecurityToken1.class).size());
    }

    @Test
    public void test_count_association_between_user_and_token() {
	final String user = config.restClientUtil().getUsername();
	config.restClientUtil().setUsername("USER-1");
	assertTrue("Access should be authorised.", tokenControllerRao.canAccess("USER-1", FirstLevelSecurityToken1.class));
	assertTrue("Access should be authorised.", tokenControllerRao.canAccess("USER-1", ThirdLevelSecurityToken1.class));
	assertFalse("Access should not be authorised.", tokenControllerRao.canAccess("USER-1", ThirdLevelSecurityToken2.class));
	config.restClientUtil().setUsername(user);
    }


    @Override
    public synchronized Restlet getRoot() {
	final Router router = new Router(getContext());

	final RouterHelper helper = new RouterHelper(DbDrivenTestCase2.injector, DbDrivenTestCase2.entityFactory);
	helper.register(router, IUserRoleDao2.class);

	final Restlet tokenRoleAssociationRestlet = new SecurityTokenResourceFactory(DbDrivenTestCase2.injector);
	router.attach("/users/{username}/securitytokens", tokenRoleAssociationRestlet);
	router.attach("/users/{username}/securitytokens/{token}", tokenRoleAssociationRestlet);
	router.attach("/users/{username}/securitytokens/{token}/useroles", tokenRoleAssociationRestlet);

	return router;
    }

    @Override
    protected String[] getDataSetPaths() {
	return new String[] { "src/test/resources/data-files/user-role-test-case.flat.xml" };
    }

}
