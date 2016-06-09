package ua.com.fielden.web.security.securityroleassociation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.ISecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchActionRao;
import ua.com.fielden.platform.security.SecurityTokenControllerRao;
import ua.com.fielden.platform.security.UserRoleRao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.factories.SecurityTokenResourceFactory;
import ua.com.fielden.platform.web.factories.TokenRoleAssociationResourceFactory;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.security.userroleaccosication.FirstLevelSecurityToken1;

public class SecurityRoleAssociationBatchActionTest extends WebBasedTestCase {

    private final IUserRoleDao userRoleRao = new UserRoleRao(config.restClientUtil());
    private final ISecurityRoleAssociationBatchAction associationRao = new SecurityRoleAssociationBatchActionRao(config.restClientUtil());
    private final ISecurityTokenController securityTokenController = new SecurityTokenControllerRao(userRoleRao, config.restClientUtil());

    @Test
    public void test_whether_security_role_batch_action_works() {
        final Map<Long, UserRole> roles = mapById(userRoleRao.findAll());

        final Set<SecurityRoleAssociation> saveAssociations = new HashSet<>();
        saveAssociations.add(config.entityFactory().newPlainEntity(SecurityRoleAssociation.class, null).setRole(roles.get(Long.valueOf(3))).setSecurityToken(FirstLevelSecurityToken1.class));

        final Set<SecurityRoleAssociation> removeAssociations = new HashSet<>();
        removeAssociations.add(config.entityFactory().newPlainEntity(SecurityRoleAssociation.class, null).setRole(roles.get(Long.valueOf(1))).setSecurityToken(FirstLevelSecurityToken1.class));

        final SecurityRoleAssociationBatchAction action = new SecurityRoleAssociationBatchAction();
        action.setSaveEntities(saveAssociations);
        action.setRemoveEntities(removeAssociations);

        associationRao.save(action);

        final Class<FirstLevelSecurityToken1> token = FirstLevelSecurityToken1.class;
        final List<UserRole> tokenRoles = securityTokenController.findUserRolesFor(token);
        
        final Map<Long, UserRole> associatedRoles = mapById(tokenRoles);
        assertTrue("Incorrect number of edited roles of the FirstLevelSecurityToken1.", associatedRoles.size() == 2);
        assertNotNull("FirstLevelSecurityToken1 must be associated with role2.", associatedRoles.get(Long.valueOf(2)));
        assertNotNull("FirstLevelSecurityToken1 must be associated with role3.", associatedRoles.get(Long.valueOf(3)));
    }

    @Test
    public void test_whether_batch_save_is_transactional() {
        final Map<Long, UserRole> roles = mapById(userRoleRao.findAll());

        final Set<SecurityRoleAssociation> saveAssociations = new HashSet<>();
        saveAssociations.add(config.entityFactory().newPlainEntity(SecurityRoleAssociation.class, null).setRole(roles.get(Long.valueOf(3))).setSecurityToken(FirstLevelSecurityToken1.class));

        final Set<SecurityRoleAssociation> removeAssociations = new HashSet<>();
        removeAssociations.add(config.entityFactory().newPlainEntity(SecurityRoleAssociation.class, null).setRole(roles.get(Long.valueOf(1))).setSecurityToken(null));

        final SecurityRoleAssociationBatchAction action = new SecurityRoleAssociationBatchAction();
        action.setSaveEntities(saveAssociations);
        action.setRemoveEntities(removeAssociations);

        try {
            associationRao.save(action);
            fail("It should fail.");
        } catch (final Exception ex) {
        }
        
        final Class<FirstLevelSecurityToken1> token = FirstLevelSecurityToken1.class;
        final List<UserRole> tokenRoles = securityTokenController.findUserRolesFor(token);
        
        final Map<Long, UserRole> associatedRoles = mapById(tokenRoles);
        assertTrue("Incorrect number of edited roles of the FirstLevelSecurityToken1.", associatedRoles.size() == 2);
        assertNotNull("FirstLevelSecurityToken1 must be associated with role1.", associatedRoles.get(Long.valueOf(1)));
        assertNotNull("FirstLevelSecurityToken1 must be associated with role2.", associatedRoles.get(Long.valueOf(2)));
    }

    /**
     * Returns the map between id and the entity with that id.
     * 
     * @param entities
     * @return
     */
    private <T extends AbstractEntity<?>> Map<Long, T> mapById(final Collection<T> entities) {
        final Map<Long, T> map = new HashMap<>();
        for (final T entity : entities) {
            map.put(entity.getId(), entity);
        }
        return map;
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());

        final RouterHelper helper = new RouterHelper(DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
        helper.register(router, IUserRoleDao.class);
        helper.register(router, IUser.class);
        helper.register(router, ISecurityRoleAssociationBatchAction.class);
        
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
