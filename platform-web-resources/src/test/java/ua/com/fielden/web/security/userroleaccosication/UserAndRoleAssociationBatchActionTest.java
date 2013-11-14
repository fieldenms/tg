package ua.com.fielden.web.security.userroleaccosication;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchActionRao;
import ua.com.fielden.platform.security.UserControllerRao;
import ua.com.fielden.platform.security.UserRoleRao;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;

public class UserAndRoleAssociationBatchActionTest extends WebBasedTestCase {

    private final IUserRoleDao userRoleRao = new UserRoleRao(config.restClientUtil());
    private final IUserController userControllerRao = new UserControllerRao(userRoleRao, config.restClientUtil());
    private final IUserAndRoleAssociationBatchAction associationRao = new UserAndRoleAssociationBatchActionRao(config.restClientUtil());

    @Test
    public void test_whether_user_and_role_batch_action_works() {
	final Map<Long, User> users = (Map<Long, User>)mapById(userControllerRao.findAllUsers());
	final Map<Long, UserRole> roles = (Map<Long, UserRole>)mapById(userRoleRao.findAll());

	final Set<UserAndRoleAssociation> saveAssociations = new HashSet<>();
	saveAssociations.add(new UserAndRoleAssociation(users.get(Long.valueOf(1)), roles.get(Long.valueOf(3))));

	final Set<UserAndRoleAssociation> removeAssociations = new HashSet<>();
	removeAssociations.add(new UserAndRoleAssociation(users.get(Long.valueOf(1)), roles.get(Long.valueOf(1))));

	final UserAndRoleAssociationBatchAction action = new UserAndRoleAssociationBatchAction();
	action.setSaveEntities(saveAssociations);
	action.setRemoveEntities(removeAssociations);

	associationRao.save(action);

	final User user = userControllerRao.findUserByIdWithRoles(Long.valueOf(1));
	final Map<Long, UserRole> associatedRoles = (Map<Long, UserRole>)mapById(user.roles());
	assertTrue("Incorrect number of edited roles of the USER-1", associatedRoles.size() == 2);
	assertNotNull("USER-1 must be associated with role2", associatedRoles.get(Long.valueOf(2)));
	assertNotNull("USER-1 must be associated with role3", associatedRoles.get(Long.valueOf(3)));
    }

    @Test
    public void test_whether_batch_save_is_transactional() {
	final Map<Long, User> users = (Map<Long, User>)mapById(userControllerRao.findAllUsers());
	final Map<Long, UserRole> roles = (Map<Long, UserRole>)mapById(userRoleRao.findAll());

	final Set<UserAndRoleAssociation> saveAssociations = new LinkedHashSet<>();
	saveAssociations.add(new UserAndRoleAssociation(users.get(Long.valueOf(1)), roles.get(Long.valueOf(3))));

	final Set<UserAndRoleAssociation> removeAssociations = new HashSet<>();
	removeAssociations.add(new UserAndRoleAssociation(null, roles.get(Long.valueOf(1))));

	final UserAndRoleAssociationBatchAction action = new UserAndRoleAssociationBatchAction();
	action.setSaveEntities(saveAssociations);
	action.setRemoveEntities(removeAssociations);

	try {
	    associationRao.save(action);
	    fail("It must have failed");
	} catch (final Exception ex) {

	}

	final User user = userControllerRao.findUserByIdWithRoles(Long.valueOf(1));
	final Map<Long, UserRole> associatedRoles = (Map<Long, UserRole>)mapById(user.roles());
	assertTrue("Incorrect number of edited roles of the USER-1", associatedRoles.size() == 2);
	assertNotNull("USER-1 must be associated with role1", associatedRoles.get(Long.valueOf(1)));
	assertNotNull("USER-1 must be associated with role2", associatedRoles.get(Long.valueOf(2)));
    }

    /**
     * Returns the map between id and the entity with that id.
     *
     * @param entities
     * @return
     */
    private Map<Long, ? extends AbstractEntity<?>> mapById(final Collection<? extends AbstractEntity<?>> entities) {
	final Map<Long, AbstractEntity<?>> map = new HashMap<>();
	for(final AbstractEntity<?> entity : entities) {
	    map.put(entity.getId(), entity);
	}
	return map;
    }

    @Override
    public synchronized Restlet getInboundRoot() {
	final Router router = new Router(getContext());

	final RouterHelper helper = new RouterHelper(DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
	helper.register(router, IUserRoleDao.class);
	helper.register(router, IUserDao.class);
	helper.register(router, IUserAndRoleAssociationBatchAction.class);

	return router;
    }


    @Override
    protected String[] getDataSetPaths() {
	return new String[] { "src/test/resources/data-files/user-role-test-case.flat.xml" };
    }
}
