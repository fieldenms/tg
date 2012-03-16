package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.DbDrivenTestCase;

import static ua.com.fielden.platform.equery.equery.select;

/**
 * Test case for the {@link IPersonDao}, {@link IUserRoleDao}, {@link IUserAndRoleAssociationDao}, and {@link SecurityRoleAssociationDao} classes
 *
 * @author TG Team
 *
 */
public class UserUserRoleTestCase extends DbDrivenTestCase {
    private final IUserRoleDao userRoleDao = injector.getInstance(IUserRoleDao.class);
    private final IUserAndRoleAssociationDao userAssociationDao = injector.getInstance(IUserAndRoleAssociationDao.class);
    private final ISecurityRoleAssociationDao securityDao = injector.getInstance(ISecurityRoleAssociationDao.class);
    private final IUserController userDao = injector.getInstance(IUserController.class);

    public void test_retrieval_of_user_role_associations() {
	final IQueryModel<UserAndRoleAssociation> associationModel = select(UserAndRoleAssociation.class).model();
	assertEquals("Incorrect number of user role associations.", 8, userAssociationDao.firstPage(associationModel, new fetch(UserAndRoleAssociation.class).with("user", new fetch(User.class)), 10).data().size());
    }

    public void test_retrieval_of_users() {
	final List<User> users = userDao.findAllUsersWithRoles();
	assertEquals("the number of retrieved persons is incorrect. Please check the testThatTheUsersWereRetrievedCorrectly", 4, users.size());

	for (int userIndex = 0; userIndex < 4; userIndex++) {
	    final User user = users.get(userIndex);
	    assertEquals("incorrect id of the " + userIndex + "-th person in the testThatTheUsersWereRetrievedCorrectly", userIndex + 1, user.getId().intValue());
	    assertEquals("incorrect key of the " + userIndex + "-th person in the testThatTheUsersWereRetrievedCorrectly", "user" + Integer.toString(userIndex + 1), user.getKey());
	    assertEquals("incorrect password of the " + userIndex + "-th person in the testThatTheUsersWereRetrievedCorrectly", "userpass" + Integer.toString(userIndex + 1), user.getPassword());

	    final Set<UserAndRoleAssociation> userRolesAssociation = user.getRoles();
	    final Set<UserRole> userRoles = new HashSet<UserRole>();
	    for (final UserAndRoleAssociation userAssociation : userRolesAssociation) {
		userRoles.add(userAssociation.getUserRole());
	    }
	    assertEquals("the " + userIndex + "-th person has wrong number of user roles, please check the testThatTheUsersWereRetrievedCorrectly", 2, userRoles.size());
	    for (int userRoleIndex = 0; userRoleIndex < 2; userRoleIndex++) {
		final int userRoleGlobalIndex = 2 * userIndex + userRoleIndex;
		final UserRole userRole = new UserRole("role" + Integer.toString(userRoleGlobalIndex + 1), "");
		assertTrue("the " + userIndex + "-th person doesn't have the " + Integer.toString(userRoleGlobalIndex + 1) + "-th user role", userRoles.contains(userRole));
	    }
	}
    }

    public void test_user_role_retrieval() {
	final List<UserRole> userRoles = userRoleDao.findAll();
	assertEquals("the number of retrieved user roles is incorrect. Please check the testThatUserRolesWereRetrievedCorrectly", 8, userRoles.size());

	for (int userRoleIndex = 0; userRoleIndex < 8; userRoleIndex++) {
	    final UserRole userRole = userRoles.get(userRoleIndex);
	    assertEquals("incorrect id of the " + userRoleIndex + "-th user role in the testThatUserRolesWereRetrievedCorrectly", userRoleIndex + 1, userRole.getId().intValue());
	    assertEquals("incorrect key of the " + userRoleIndex + "-th user role in the testThatUserRolesWereRetrievedCorrectly", "role" + Integer.toString(userRoleIndex + 1), userRole.getKey());
	    assertEquals("incorrect description of the " + userRoleIndex + "-th user role in the testThatUserRolesWereRetrievedCorrectly", "role desc "
		    + Integer.toString(userRoleIndex + 1), userRole.getDesc());
	}
    }

    public void test_user_role_retrieval_by_ids() {
	final List<UserRole> userRoles = userRoleDao.findByIds(1L, 4L);
	assertEquals("Incorrect number of user roles.", 2, userRoles.size());
    }

    public void test_that_save_for_users() {
	config.getHibernateUtil().getSessionFactory().getCurrentSession().close();
	// retrieving the user, modifying it's password and saving changes
	User user = userDao.findUserByIdWithRoles(1L);
	user.setPassword("new password");
	UserAndRoleAssociation userAssociation = entityFactory.newByKey(UserAndRoleAssociation.class, user, new UserRole("role1", ""));
	final List<UserAndRoleAssociation> associations = new ArrayList<UserAndRoleAssociation>();
	for (final UserAndRoleAssociation roleAssociation : user.getRoles()) {
	    if (roleAssociation.equals(userAssociation)) {
		associations.add(roleAssociation);
	    }
	}
	userAssociationDao.removeAssociation(associations);
	userDao.save(user);

	// retrieving saved user and checking it
	user = userDao.findUserByIdWithRoles(1L);
	assertEquals("incorrect id of the first person in the testWhetherTheSaveWorksProperlyForUsers", 1, user.getId().intValue());
	assertEquals("incorrect key of the first person in the testWhetherTheSaveWorksProperlyForUsers", "user" + Integer.toString(1), user.getKey());
	assertEquals("incorrect password of the first person in the testWhetherTheSaveWorksProperlyForUsers", "new password", user.getPassword());

	// checking whether the user role1 was removed or not
	final Set<UserAndRoleAssociation> userRoleAssociations = user.getRoles();
	assertEquals("the first person has wrong number of user roles, please check the testWhetherTheSaveWorksProperlyForUsers", 1, userRoleAssociations.size());
	userAssociation = entityFactory.newByKey(UserAndRoleAssociation.class, user, new UserRole("role2", ""));
	assertTrue("the " + 1 + "-th person doesn't have the second user role", userRoleAssociations.contains(userAssociation));

    }

    public void test_whether_the_created_user_were_correctly_saved() {
	config.getHibernateUtil().getSessionFactory().getCurrentSession().close();

	// creating new person and user roles for it. Saving person
	final UserRole userRole1 = entityFactory.newEntity(UserRole.class, "nrole1", "nrole desc 1");
	userRoleDao.save(userRole1);
	final UserRole userRole2 = entityFactory.newEntity(UserRole.class, "nrole2", "nrole desc 2");
	userRoleDao.save(userRole2);
	final UserRole userRole3 = entityFactory.newEntity(UserRole.class, "nrole3", "nrole desc 3");
	userRoleDao.save(userRole3);

	User user = entityFactory.newEntity(User.class, "new user", "new user desc");
	user.setPassword("new user password");
	userDao.save(user);

	Set<UserAndRoleAssociation> userRolesAssociation = new HashSet<UserAndRoleAssociation>();
	userRolesAssociation.add(entityFactory.newByKey(UserAndRoleAssociation.class, user, userRole1));
	userRolesAssociation.add(entityFactory.newByKey(UserAndRoleAssociation.class, user, userRole2));
	userRolesAssociation.add(entityFactory.newByKey(UserAndRoleAssociation.class, user, userRole3));
	user.setRoles(userRolesAssociation);

	for (final UserAndRoleAssociation association : userRolesAssociation) {
	    userAssociationDao.save(association);
	}

	// final checking weather the final person was saved final correctly with user final roles
	user = userDao.findUserByKeyWithRoles("new user");
	assertNotNull("Saved user should have been found.", user);
	assertEquals("incorrect password of the 'new user' person in the testWhetherTheCreatedUserWereCorrectlySaved", "new user password", user.getPassword());

	// checking whether the user roles were saved correctly
	userRolesAssociation = user.getRoles();
	assertEquals("the 'new user' person has wrong number of user roles, please check the testWhetherTheCreatedUserWereCorrectlySaved", 3, userRolesAssociation.size());
	for (int userRoleIndex = 0; userRoleIndex < 3; userRoleIndex++) {
	    final UserAndRoleAssociation userRoleAssociation = entityFactory.newByKey(UserAndRoleAssociation.class, user, new UserRole("nrole"
		    + Integer.toString(userRoleIndex + 1), ""));
	    assertTrue("the 'new user'-th person doesn't have the " + userRoleAssociation.getUserRole().getKey() + "-th user role", userRolesAssociation.contains(userRoleAssociation));
	}
    }

    public void test_that_security_associations_can_be_retrieved() {
	final IQueryModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class).model();
	final List<SecurityRoleAssociation> associations = securityDao.firstPage(model, new fetch(SecurityRoleAssociation.class).with("role"), Integer.MAX_VALUE).data();
	assertEquals("incorrect number of security token - role associations", 12, associations.size());
	final List<SecurityRoleAssociation> roles = securityDao.findAssociationsFor(FirstLevelSecurityToken1.class);
	assertEquals("Incorrect number of user roles for the " + FirstLevelSecurityToken1.class.getName() + " security token", 2, roles.size());
	UserRole role = config.getEntityFactory().newByKey(UserRole.class, "role1");
	assertEquals("incorrect first role of the association", role, roles.get(0).getRole());
	role = config.getEntityFactory().newByKey(UserRole.class, "role2");
	assertEquals("incorrect second role of the association", role, roles.get(1).getRole());
    }

    public void test_that_new_security_role_association_can_be_saved() {
	final UserRole role = config.getEntityFactory().newEntity(UserRole.class, "role56", "role56 desc");
	final SecurityRoleAssociation association = config.getEntityFactory().newByKey(SecurityRoleAssociation.class, FirstLevelSecurityToken1.class, role);
	userRoleDao.save(role);
	securityDao.save(association);
	final List<SecurityRoleAssociation> roles = securityDao.findAssociationsFor(FirstLevelSecurityToken1.class);
	assertEquals("Incorrect number of user roles for the " + FirstLevelSecurityToken1.class.getName() + " security token", 3, roles.size());
	assertTrue("The " + FirstLevelSecurityToken1.class.getName() + " security token doesn't have a role56 user role", roles.contains(association));
    }

    public void test_that_security_role_association_can_be_removed() {
	List<SecurityRoleAssociation> roles = securityDao.findAssociationsFor(FirstLevelSecurityToken1.class);
	assertEquals("Incorrect number of user roles for the " + FirstLevelSecurityToken1.class.getName() + " security token", 2, roles.size());
	securityDao.removeAssociationsFor(FirstLevelSecurityToken1.class);
	roles = securityDao.findAssociationsFor(FirstLevelSecurityToken1.class);
	assertEquals("Incorrect number of user roles for the " + FirstLevelSecurityToken1.class.getName() + " security token", 0, roles.size());
    }

    //TODO fix
    public void test_count_association_between_user_and_token() {
	assertEquals("Incorrect number of associations between user and token.", 2, securityDao.countAssociations("user1", FirstLevelSecurityToken1.class));
	assertEquals("Incorrect number of associations between user and token.", 2, securityDao.countAssociations("user1", ThirdLevelSecurityToken1.class));
	assertEquals("Incorrect number of associations between user and token.", 0, securityDao.countAssociations("user1", ThirdLevelSecurityToken2.class));
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/user-user_role-test-case.flat.xml" };
    }

}
