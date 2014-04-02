package ua.com.fielden.web.security.userroleaccosication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.security.UserControllerRao;
import ua.com.fielden.platform.security.UserRoleRao;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.UserRoleAssociationResourceFactory;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;

/**
 * Provides a unit test for user/role management.
 * 
 * @author TG Team
 * 
 */
public class UserAndRoleAssociationManagementTestCase extends WebBasedTestCase {

    private final IUserRoleDao userRoleRao = new UserRoleRao(config.restClientUtil());
    private final IUserController userControllerRao = new UserControllerRao(userRoleRao, config.restClientUtil());

    @Test
    public void test_that_all_user_roles_can_be_found() {
        final List<? extends UserRole> roles = userControllerRao.findAllUserRoles();
        assertEquals("Incorrect number of roles.", 8, roles.size());
    }

    @Test
    public void test_collection_correctness() {
        final List<User> users = userControllerRao.findAllUsersWithRoles();
        final User user = users.get(0);
        final UserAndRoleAssociation role1 = user.getRoles().iterator().next();
        assertTrue(user.getRoles().contains(role1));
    }

    @Test
    public void test_that_all_users_can_be_found() {
        final List<? extends User> users = userControllerRao.findAllUsers();
        assertEquals("Incorrect number of user.", 4, users.size());
    }

    @Test
    public void test_that_all_users_have_roles() {
        final List<? extends User> users = userControllerRao.findAllUsers();
        for (final User user : users) {
            assertEquals("Incorrect number of roles for user " + user, 2, user.getRoles().size());
        }
    }

    @Test
    public void test_that_user_role_associations_can_be_updated_by_substituting_existing_associaitons() {
        final User user = userControllerRao.findAllUsers().get(0);
        assertEquals("Unexpected first user.", "USER-1", user.getKey());
        final UserRole role = userControllerRao.findAllUserRoles().get(2);
        assertEquals("Unexpected third role.", "role3", role.getKey());

        userControllerRao.updateUsers(new HashMap<User, Set<UserRole>>() {
            {
                put(user, new HashSet<UserRole>() {
                    {
                        add(role);
                    }
                });
            }
        });
        assertEquals("Incorrect number of roles.", 1, userControllerRao.findAllUsers().get(0).getRoles().size());
    }

    @Test
    public void test_that_user_role_associations_can_be_updated_by_adding_new_association() {
        final User user = userControllerRao.findAllUsers().get(0);
        assertEquals("Unexpected first user.", "USER-1", user.getKey());
        final UserRole role = userControllerRao.findAllUserRoles().get(2);
        assertEquals("Unexpected third role.", "role3", role.getKey());

        userControllerRao.updateUsers(new HashMap<User, Set<UserRole>>() {
            {
                put(user, new HashSet<UserRole>(user.roles()) {
                    {
                        add(role);
                    }
                });
            }
        });
        assertEquals("Incorrect number of roles.", 3, userControllerRao.findAllUsers().get(0).getRoles().size());
    }

    @Test
    public void test_that_user_role_associations_can_be_updated_by_removing_associations() {
        final User user = userControllerRao.findAllUsers().get(0);
        assertEquals("Unexpected first user.", "USER-1", user.getKey());
        final UserRole role = userControllerRao.findAllUserRoles().get(2);
        assertEquals("Unexpected third role.", "role3", role.getKey());

        userControllerRao.updateUsers(new HashMap<User, Set<UserRole>>() {
            {
                put(user, new HashSet<UserRole>());
            }
        });
        assertEquals("Incorrect number of roles.", 0, userControllerRao.findAllUsers().get(0).getRoles().size());
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());

        final RouterHelper helper = new RouterHelper(DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
        helper.register(router, IUserRoleDao.class);
        helper.register(router, IUserDao.class);

        final Restlet userRoleAssociationRestlet = new UserRoleAssociationResourceFactory(DbDrivenTestCase.injector);
        router.attach("/users/{username}/useroles", userRoleAssociationRestlet);

        return router;
    }

    @Override
    protected String[] getDataSetPaths() {
        return new String[] { "src/test/resources/data-files/user-role-test-case.flat.xml" };
    }

}
