package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserAndRoleAssociationCo;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRoleCo;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * Test case for the {@link UserRoleCo}, {@link UserAndRoleAssociationCo}, and {@link SecurityRoleAssociationDao} classes
 * 
 * @author TG Team
 * 
 */
public class UserUserRoleTestCase extends AbstractDaoTestCase {
    private final UserRoleCo coUserRole = getInstance(UserRoleCo.class);
    private final UserAndRoleAssociationCo coUserAndRoleAssociation = getInstance(UserAndRoleAssociationCo.class);
    private final SecurityRoleAssociationCo coSecurityRoleAssociation = getInstance(SecurityRoleAssociationCo.class);
    private final IUser coUser = getInstance(IUser.class);

    @Test
    public void user_role_associations_can_be_retrieved() {
        final EntityResultQueryModel<UserAndRoleAssociation> associationModel = select(UserAndRoleAssociation.class).model();
        assertEquals("Incorrect number of user role associations.", 9, coUserAndRoleAssociation.firstPage(from(associationModel).with(fetch(UserAndRoleAssociation.class).with("user", fetch(User.class))).model(), 10).data().size());
    }

    @Test
    public void users_can_be_retrived_together_with_their_roles() {
        final List<User> users = coUser.findAllUsersWithRoles();
        assertEquals("the number of retrieved persons is incorrect. Please check the testThatTheUsersWereRetrievedCorrectly", 5, users.size());

        for (int userIndex = 0; userIndex < 4; userIndex++) {
            final User user = users.get(userIndex);
            if (UNIT_TEST_USER.equals(user.getKey())) {
                continue;
            }
            
            assertEquals("incorrect key of the " + userIndex + "-th person in the testThatTheUsersWereRetrievedCorrectly", "user" + Integer.toString(userIndex), user.getKey());

            final Set<UserAndRoleAssociation> userRolesAssociation = user.getRoles();
            final Set<UserRole> userRoles = new HashSet<>();
            for (final UserAndRoleAssociation userAssociation : userRolesAssociation) {
                userRoles.add(userAssociation.getUserRole());
            }
            assertEquals("the " + userIndex + "-th person has wrong number of user roles, please check the testThatTheUsersWereRetrievedCorrectly", 2, userRoles.size());
            for (int userRoleIndex = 0; userRoleIndex < 2; userRoleIndex++) {
                final int userRoleGlobalIndex = 2 * userIndex + userRoleIndex;
                final UserRole userRole = new_(UserRole.class, "role" + Integer.toString(userRoleGlobalIndex - 1), "");
                
                assertTrue("the " + userIndex + "-th person doesn't have the " + Integer.toString(userRoleGlobalIndex + 1) + "-th user role", userRoles.contains(userRole));
            }
        }
    }

    @Test
    public void all_user_roles_can_be_identified() {
        final List<UserRole> userRoles = coUserRole.findAll();
        assertEquals("the number of retrieved user roles is incorrect. Please check the testThatUserRolesWereRetrievedCorrectly", 9, userRoles.size());

        for (int userRoleIndex = 0; userRoleIndex < 9; userRoleIndex++) {
            final UserRole userRole = userRoles.get(userRoleIndex);
            if (!UNIT_TEST_ROLE.equals(userRole.getKey())) {
                assertEquals("incorrect key of the " + userRoleIndex + "-th user role in the testThatUserRolesWereRetrievedCorrectly", "role" + Integer.toString(userRoleIndex), userRole.getKey());
                assertEquals("incorrect description of the " + userRoleIndex + "-th user role in the testThatUserRolesWereRetrievedCorrectly", "role desc " + Integer.toString(userRoleIndex), userRole.getDesc());
            }
        }
    }

    @Test
    public void various_manipulations_with_user_and_roles_works_as_expected() {
        // retrieving the user, modifying it's email
        final User userBefore = coUser.findUserByKeyWithRoles("user1");
        // we have two associations for user1: role1 and role2
        assertEquals(2, userBefore.getRoles().size());
        userBefore.setEmail("new_email@gmail.com");

        // looking for association between user1 and role1
        final UserRole role1 = co(UserRole.class).findByKey("role1");
        final UserAndRoleAssociation userAssociation = co(UserAndRoleAssociation.class).findByKey(userBefore, role1);
        assertNotNull(userAssociation);

        // removing this association between user1 and role1
        final Set<UserAndRoleAssociation> associations = new HashSet<>();
        for (final UserAndRoleAssociation roleAssociation : userBefore.getRoles()) {
            if (roleAssociation.equals(userAssociation)) {
                associations.add(roleAssociation);
            }
        }
        assertEquals(1, associations.size());
        coUserAndRoleAssociation.removeAssociation(associations);
        coUser.save(userBefore);

        // retrieving saved user and checking it
        final User userAfter = coUser.findUserByKeyWithRoles("user1");
        assertEquals("incorrect key of the first person in the testWhetherTheSaveWorksProperlyForUsers", "user" + Integer.toString(1), userAfter.getKey());
        assertEquals("incorrect password of the first person in the testWhetherTheSaveWorksProperlyForUsers", "new_email@gmail.com", userAfter.getEmail());

        // checking whether the user role1 was removed or not
        final Set<UserAndRoleAssociation> userRoleAssociations = userAfter.getRoles();
        assertEquals("the first person has wrong number of user roles, please check the testWhetherTheSaveWorksProperlyForUsers", 1, userRoleAssociations.size());
        final UserRole role2 = co(UserRole.class).findByKey("role2");
        final UserAndRoleAssociation userAssociationRole2 = co(UserAndRoleAssociation.class).findByKey(userAfter, role2);
        assertTrue("the 1st person doesn't have the second user role", userRoleAssociations.contains(userAssociationRole2));
    }

    @Test
    public void created_user_are_saved_together_with_their_roles() {
        // creating new person
        final UserRole userRole1 = save(new_(UserRole.class, "nrole1", "nrole desc 1"));
        final UserRole userRole2 = save(new_(UserRole.class, "nrole2", "nrole desc 2"));
        final UserRole userRole3 = save(new_(UserRole.class, "nrole3", "nrole desc 3"));

        final String newUserName = "new_user";
        User user = save(new_(User.class, newUserName, "new user desc").setBase(true).setEmail("new_email@gmail.com"));

        // assigning 3 roles for this new_user and saving them
        Set<UserAndRoleAssociation> userRolesAssociation = new HashSet<>();
        userRolesAssociation.add(new_composite(UserAndRoleAssociation.class, user, userRole1));
        userRolesAssociation.add(new_composite(UserAndRoleAssociation.class, user, userRole2));
        userRolesAssociation.add(new_composite(UserAndRoleAssociation.class, user, userRole3));

        for (final UserAndRoleAssociation association : userRolesAssociation) {
            coUserAndRoleAssociation.save(association);
        }

        // finally checking whether the person was saved correctly with all it's user roles
        user = coUser.findUserByKeyWithRoles(newUserName);
        assertNotNull("Saved user should have been found.", user);
        assertEquals("incorrect password of the 'new user' person in the testWhetherTheCreatedUserWereCorrectlySaved", "new_email@gmail.com", user.getEmail());

        // checking whether the user roles were saved correctly
        userRolesAssociation = user.getRoles();
        assertEquals("the 'new user' person has wrong number of user roles, please check the testWhetherTheCreatedUserWereCorrectlySaved", 3, userRolesAssociation.size());
        for (int userRoleIndex = 0; userRoleIndex < 3; userRoleIndex++) {
            final UserAndRoleAssociation userRoleAssociation = co(UserAndRoleAssociation.class).findByKey(user, co(UserRole.class).findByKey("nrole" + Integer.toString(userRoleIndex + 1)));
            assertTrue("the 'new user'-th person doesn't have the " + userRoleAssociation.getUserRole().getKey() + "-th user role", userRolesAssociation.contains(userRoleAssociation));
        }
    }

    @Test
    public void security_associations_can_be_retrieved() {
        final EntityResultQueryModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class).model();
        final List<SecurityRoleAssociation> associations = coSecurityRoleAssociation.firstPage(from(model).with(fetch(SecurityRoleAssociation.class).with("role")).model(), Integer.MAX_VALUE).data();
        assertEquals("incorrect number of security token - role associations", 75, associations.size());
        final List<SecurityRoleAssociation> roles = coSecurityRoleAssociation.findAssociationsFor(FirstLevelSecurityToken1.class);
        assertEquals("Incorrect number of user roles for the " + FirstLevelSecurityToken1.class.getName() + " security token", 2, roles.size());
        UserRole role = new_(UserRole.class, "role1");
        assertEquals("incorrect first role of the association", role, roles.get(0).getRole());
        role = new_(UserRole.class, "role2");
        assertEquals("incorrect second role of the association", role, roles.get(1).getRole());
    }

    @Test
    public void new_security_role_association_can_be_saved() {
        final UserRole role = save(new_(UserRole.class, "role56", "role56 desc").setActive(true));
        final SecurityRoleAssociation association = save(new_composite(SecurityRoleAssociation.class, FirstLevelSecurityToken1.class, role));
        final List<SecurityRoleAssociation> roles = coSecurityRoleAssociation.findAssociationsFor(FirstLevelSecurityToken1.class);
        assertEquals("Incorrect number of user roles for the " + FirstLevelSecurityToken1.class.getName() + " security token", 3, roles.size());
        assertTrue("The " + FirstLevelSecurityToken1.class.getName() + " security token doesn't have a role56 user role", roles.contains(association));
    }

    @Test
    public void count_associations_between_users_and_tokens_takes_into_account_active_association() {
        final IUser coUser = co$(User.class);
        assertEquals("Incorrect number of associations between user and token.", 2, coSecurityRoleAssociation.countActiveAssociations(coUser.findByKey("user1"), FirstLevelSecurityToken1.class));
        assertEquals("Incorrect number of associations between user and token.", 2, coSecurityRoleAssociation.countActiveAssociations(coUser.findByKey("user1"), ThirdLevelSecurityToken1.class));
        assertEquals("Incorrect number of associations between user and token.", 0, coSecurityRoleAssociation.countActiveAssociations(coUser.findByKey("user1"), ThirdLevelSecurityToken2.class));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final UserRole role1 = save(new_(UserRole.class, "role1", "role desc 1").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "role2", "role desc 2").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "role3", "role desc 3").setActive(true));
        final UserRole role4 = save(new_(UserRole.class, "role4", "role desc 4").setActive(true));
        final UserRole role5 = save(new_(UserRole.class, "role5", "role desc 5").setActive(true));
        final UserRole role6 = save(new_(UserRole.class, "role6", "role desc 6").setActive(true));
        final UserRole role7 = save(new_(UserRole.class, "role7", "role desc 7").setActive(true));
        final UserRole role8 = save(new_(UserRole.class, "role8", "role desc 8").setActive(true));

        final User user1 = save(new_(User.class, "user1", "user desc 1").setBase(true));
        final User user2 = save(new_(User.class, "user2", "user desc 2").setBase(true));
        final User user3 = save(new_(User.class, "user3", "user desc 3").setBase(true));
        final User user4 = save(new_(User.class, "user4", "user desc 4").setBase(true));

        save(new_composite(UserAndRoleAssociation.class, user1, role1));
        save(new_composite(UserAndRoleAssociation.class, user1, role2));
        save(new_composite(UserAndRoleAssociation.class, user2, role3));
        save(new_composite(UserAndRoleAssociation.class, user2, role4));
        save(new_composite(UserAndRoleAssociation.class, user3, role5));
        save(new_composite(UserAndRoleAssociation.class, user3, role6));
        save(new_composite(UserAndRoleAssociation.class, user4, role7));
        save(new_composite(UserAndRoleAssociation.class, user4, role8));

        save(new_composite(SecurityRoleAssociation.class).setRole(role1).setSecurityToken(FirstLevelSecurityToken1.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role2).setSecurityToken(FirstLevelSecurityToken1.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role3).setSecurityToken(FirstLevelSecurityToken2.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role4).setSecurityToken(FirstLevelSecurityToken2.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role5).setSecurityToken(SecondLevelSecurityToken1.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role6).setSecurityToken(SecondLevelSecurityToken1.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role7).setSecurityToken(SecondLevelSecurityToken2.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role8).setSecurityToken(SecondLevelSecurityToken2.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role1).setSecurityToken(ThirdLevelSecurityToken1.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role2).setSecurityToken(ThirdLevelSecurityToken1.class));
        save(new_composite(SecurityRoleAssociation.class).setRole(role3).setSecurityToken(ThirdLevelSecurityToken2.class));
    }

}