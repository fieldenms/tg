package ua.com.fielden.platform.dao;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.tokens.*;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.security.user.SecurityRoleAssociation.ROLE;
import static ua.com.fielden.platform.test_utils.CollectionTestUtils.assertEqualByContents;

/// A test case for user and role, and role and security token associations.
///
public class UserAndRoleAndTokenAssociationTestCase extends AbstractDaoTestCase {
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
    public void users_can_be_retrieved_together_with_their_roles() {
        final List<User> users = coUser.findAllUsersWithRoles();
        assertEquals(5, users.size());

        for (int userIndex = 0; userIndex < 4; userIndex++) {
            final User user = users.get(userIndex);
            if (UNIT_TEST_USER.equals(user.getKey())) {
                continue;
            }

            assertEquals("Incorrect key of the " + userIndex + "-th person.", "USER" + Integer.toString(userIndex), user.getKey());

            final Set<UserAndRoleAssociation> userRolesAssociation = user.getRoles();
            final Set<UserRole> userRoles = new HashSet<>();
            for (final UserAndRoleAssociation userAssociation : userRolesAssociation) {
                userRoles.add(userAssociation.getUserRole());
            }
            assertEquals("The " + userIndex + "-th person has wrong number of user roles.", 2, userRoles.size());
            for (int userRoleIndex = 0; userRoleIndex < 2; userRoleIndex++) {
                final int userRoleGlobalIndex = 2 * userIndex + userRoleIndex;
                final UserRole userRole = new_(UserRole.class, "ROLE" + Integer.toString(userRoleGlobalIndex - 1), "");

                assertTrue("The " + userIndex + "-th person doesn't have the " + Integer.toString(userRoleGlobalIndex + 1) + "-th user role.", userRoles.contains(userRole));
            }
        }
    }

    @Test
    public void all_user_roles_can_be_identified() {
        final List<UserRole> userRoles = coUserRole.findAll();
        assertEquals(9, userRoles.size());

        for (int userRoleIndex = 0; userRoleIndex < 9; userRoleIndex++) {
            final UserRole userRole = userRoles.get(userRoleIndex);
            if (!UNIT_TEST_ROLE.equals(userRole.getKey())) {
                assertEquals("Incorrect key of the " + userRoleIndex + "-th user role", "ROLE" + Integer.toString(userRoleIndex + 1), userRole.getKey());
                assertEquals("Incorrect description of the " + userRoleIndex + "-th user role", "role desc " + Integer.toString(userRoleIndex + 1), userRole.getDesc());
            }
        }
    }

    @Test
    public void various_manipulations_with_user_and_roles_works_as_expected() {
        // retrieving the user, modifying it's email
        final User userBefore = coUser.findUserByKeyWithRoles("USER1");
        // we have 2 associations for user1: role1 and role2
        assertEquals(2, userBefore.getRoles().size());
        userBefore.setEmail("new_email@gmail.com");

        // looking for association between user1 and role1
        final UserRole role1 = co(UserRole.class).findByKey("ROLE1");
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

        // retrieve and check the updated user
        final User userAfter = coUser.findUserByKeyWithRoles("USER1");
        assertEquals("USER1", userAfter.getKey());
        assertEquals("new_email@gmail.com", userAfter.getEmail());

        // checking whether the user role1 was removed or not
        final Set<UserAndRoleAssociation> userRoleAssociations = userAfter.getRoles();
        assertEquals("Unexpected number of roles.", 1, userRoleAssociations.size());
        assertEquals("Invalid role association.", co(UserRole.class).findByKey("ROLE2"), userRoleAssociations.iterator().next().getUserRole());
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
        assertEquals("new_email@gmail.com", user.getEmail());

        // checking whether the user roles were saved correctly
        userRolesAssociation = user.getRoles();
        assertEquals(3, userRolesAssociation.size());
        for (int userRoleIndex = 0; userRoleIndex < 3; userRoleIndex++) {
            final UserAndRoleAssociation userRoleAssociation = co(UserAndRoleAssociation.class).findByKey(user, co(UserRole.class).findByKey("nrole" + Integer.toString(userRoleIndex + 1)));
            assertTrue("The 'new user'-th person doesn't have the " + userRoleAssociation.getUserRole().getKey() + "-th user role.", userRolesAssociation.contains(userRoleAssociation));
        }
    }

    @Test
    public void security_associations_can_be_retrieved() {
        final EntityResultQueryModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class).model();
        final List<SecurityRoleAssociation> associations = coSecurityRoleAssociation.getAllEntities(from(model).with(fetch(SecurityRoleAssociation.class).with("role")).model());
        assertThat(associations)
                .describedAs(() -> "Incorrect number of security token/role associations.")
                .hasSize(117);
        final List<SecurityRoleAssociation> roles = coSecurityRoleAssociation.findAssociationsFor(FirstLevelSecurityToken1.class);
        assertEqualByContents(Set.of(UNIT_TEST_ROLE, "ROLE1", "ROLE2"),
                              roles.stream().map(SecurityRoleAssociation::getRole).map(UserRole::getKey).collect(toImmutableSet()));;
    }

    @Test
    public void new_security_role_association_can_be_saved() {
        final UserRole role = save(new_(UserRole.class, "ROLE56", "role56 desc").setActive(true));
        final SecurityRoleAssociation association = save(new_composite(SecurityRoleAssociation.class, FirstLevelSecurityToken1.class, role));
        final List<SecurityRoleAssociation> roles = coSecurityRoleAssociation.findAssociationsFor(FirstLevelSecurityToken1.class);
        assertEquals("Incorrect number of user roles for the " + FirstLevelSecurityToken1.class.getName() + " security token.", 4, roles.size());
        assertTrue("The " + FirstLevelSecurityToken1.class.getName() + " security token doesn't have a role56 user role.", roles.contains(association));
    }

    @Test
    public void only_active_security_role_association_can_be_retrieved() {
        final UserRole role = save(new_(UserRole.class, "ROLE56", "role56 desc").setActive(true));
        final SecurityRoleAssociation association = save(new_composite(SecurityRoleAssociation.class, FirstLevelSecurityToken1.class, role));
        save(new_composite(SecurityRoleAssociation.class, FirstLevelSecurityToken2.class, role).setActive(false));
        final List<SecurityRoleAssociation> roles = coSecurityRoleAssociation.findAssociationsFor(FirstLevelSecurityToken1.class);
        assertEquals("Incorrect number of user roles for the " + FirstLevelSecurityToken1.class.getName() + " security token.", 4, roles.size());
        assertTrue("The " + FirstLevelSecurityToken1.class.getName() + " security token doesn't have a role56 user role.", roles.contains(association));
    }

    @Test
    public void count_associations_between_users_and_tokens_takes_into_account_active_association() {
        // Find user1.
        final IUser coUser = co$(User.class);
        final User user1 = coUser.findUserByKeyWithRoles("USER1");
        // Create new ROLE9.
        final UserRole role9 = save(new_(UserRole.class, "ROLE9", "role desc 9").setActive(true));
        // Associate new role9 with USER1.
        save(new_composite(UserAndRoleAssociation.class, user1, role9));
        // Create inactive security token - role association.
        save(new_composite(SecurityRoleAssociation.class).setRole(role9).setSecurityToken(FirstLevelSecurityToken1.class).setActive(false));
        // Test how many user tokens are associated with .
        assertEquals("Incorrect number of associations between user and token.", 2, coSecurityRoleAssociation.countActiveAssociations(coUser.findByKey("user1"), FirstLevelSecurityToken1.class));
        assertEquals("Incorrect number of associations between user and token.", 2, coSecurityRoleAssociation.countActiveAssociations(coUser.findByKey("user1"), ThirdLevelSecurityToken1.class));
        assertEquals("Incorrect number of associations between user and token.", 0, coSecurityRoleAssociation.countActiveAssociations(coUser.findByKey("user1"), ThirdLevelSecurityToken2.class));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UserRole role1 = save(new_(UserRole.class, "ROLE1", "role desc 1").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "role desc 2").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "role desc 3").setActive(true));
        final UserRole role4 = save(new_(UserRole.class, "ROLE4", "role desc 4").setActive(true));
        final UserRole role5 = save(new_(UserRole.class, "ROLE5", "role desc 5").setActive(true));
        final UserRole role6 = save(new_(UserRole.class, "ROLE6", "role desc 6").setActive(true));
        final UserRole role7 = save(new_(UserRole.class, "ROLE7", "role desc 7").setActive(true));
        final UserRole role8 = save(new_(UserRole.class, "ROLE8", "role desc 8").setActive(true));

        final User user1 = save(new_(User.class, "USER1", "user desc 1").setBase(true));
        final User user2 = save(new_(User.class, "USER2", "user desc 2").setBase(true));
        final User user3 = save(new_(User.class, "USER3", "user desc 3").setBase(true));
        final User user4 = save(new_(User.class, "USER4", "user desc 4").setBase(true));

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
