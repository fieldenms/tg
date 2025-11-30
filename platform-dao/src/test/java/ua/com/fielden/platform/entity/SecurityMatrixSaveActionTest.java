package ua.com.fielden.platform.entity;

import org.junit.Test;
import ua.com.fielden.platform.security.tokens.FirstLevelSecurityToken1;
import ua.com.fielden.platform.security.tokens.FirstLevelSecurityToken2;
import ua.com.fielden.platform.security.tokens.SecondLevelSecurityToken1;
import ua.com.fielden.platform.security.tokens.SecondLevelSecurityToken2;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanRead_Token;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanSave_Token;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRoleCo;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.SecurityMatrixSaveActionDao.ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_READING;
import static ua.com.fielden.platform.entity.SecurityMatrixSaveActionDao.ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_SAVING;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;

public class SecurityMatrixSaveActionTest extends AbstractDaoTestCase {

    private static final String TEST_ROLE_1 = "UNIT_TEST_ROLE_1";
    private static final String TEST_ROLE_2 = "UNIT_TEST_ROLE_2";
    private static final String TEST_ROLE_3 = "UNIT_TEST_ROLE_3";
    private static final String TEST_ROLE_4 = "UNIT_TEST_ROLE_4";

    @Test
    public void new_security_role_association_becomes_active() {
        final UserRoleCo userRoleCo = co(UserRole.class);
        final UserRole test_role_3 = userRoleCo.findByKey(TEST_ROLE_3);
        // Find the desired association.
        final SecurityRoleAssociationCo associationCo = co(SecurityRoleAssociation.class);
        final SecurityRoleAssociation association = associationCo.findByKey(SecondLevelSecurityToken1.class, test_role_3);
        // Verify that the desired association doesn't exist.
        assertNull(association);
        // Create the missing desired association and save it using SecurityMatrixSaveAction.
        final var associationsToSave = new HashMap<String, List<Integer>>();
        associationsToSave.put(SecondLevelSecurityToken1.class.getName(), List.of(test_role_3.getId().intValue()));
        final SecurityMatrixSaveActionCo securityMatrixSaveCo = co(SecurityMatrixSaveAction.class);
        save(securityMatrixSaveCo.new_()
                .setAssociationsToSave(associationsToSave));
        // Find the desired association again.
        final SecurityRoleAssociation refetchedAssociation = associationCo.findByKeyAndFetch(
                fetchNone(SecurityRoleAssociation.class).with(ACTIVE),
                SecondLevelSecurityToken1.class,
                test_role_3);
        // Verify that the desired association exists and is active.
        assertNotNull(refetchedAssociation);
        assertTrue(refetchedAssociation.isActive());
    }

    @Test
    public void removed_security_role_association_becomes_inactive() {
        final UserRoleCo userRoleCo = co(UserRole.class);
        final UserRole test_role_3 = userRoleCo.findByKey(TEST_ROLE_3);
        // Create a new association and save it using SecurityMatrixSaveAction.
        final var associations = new HashMap<String, List<Integer>>();
        associations.put(SecondLevelSecurityToken1.class.getName(), List.of(test_role_3.getId().intValue()));
        final SecurityMatrixSaveActionCo securityMatrixSaveCo = co(SecurityMatrixSaveAction.class);
        save(securityMatrixSaveCo.new_()
                .setAssociationsToSave(associations));
        // Remove the previously saved association using SecurityMatrixSaveAction.
        save(securityMatrixSaveCo.new_()
                .setAssociationsToRemove(associations));
        // Try to find the removed association.
        final SecurityRoleAssociationCo associationCo = co(SecurityRoleAssociation.class);
        final SecurityRoleAssociation association = associationCo.findByKey(SecondLevelSecurityToken1.class, test_role_3);
        // Verify that the removed association exist but is inactive.
        assertNotNull(association);
        assertFalse(association.isActive());
    }

    @Test
    public void added_again_security_role_association_becomes_active() {
        final UserRoleCo userRoleCo = co(UserRole.class);
        final UserRole test_role_3 = userRoleCo.findByKey(TEST_ROLE_3);
        // Find the desired association.
        final SecurityRoleAssociationCo associationCo = co(SecurityRoleAssociation.class);
        final SecurityRoleAssociation association = associationCo.findByKey(SecondLevelSecurityToken1.class, test_role_3);
        // Verify that the desired association doesn't exist.
        assertNull(association);
        // Configure the association to test.
        final var associations = new HashMap<String, List<Integer>>();
        associations.put(SecondLevelSecurityToken1.class.getName(), List.of(test_role_3.getId().intValue()));
        //  Save the association, remove it, then add it again to verify its active status.
        final SecurityMatrixSaveActionCo securityMatrixSaveCo = co(SecurityMatrixSaveAction.class);
        save(securityMatrixSaveCo.new_()
                .setAssociationsToSave(associations));
        save(securityMatrixSaveCo.new_()
                .setAssociationsToRemove(associations));
        save(securityMatrixSaveCo.new_()
                .setAssociationsToSave(associations));
        // Try to find the re-added association.
        final SecurityRoleAssociation refetchedAssociation = associationCo.findByKey(SecondLevelSecurityToken1.class, test_role_3);
        // Verify that the re-added association exists and is active.
        assertNotNull(refetchedAssociation);
        assertTrue(refetchedAssociation.isActive());
    }

    @Test
    public void security_matrix_shows_only_active_security_role_associations() {
        final SecurityMatrixSaveActionCo securityMatrixSaveCo = co(SecurityMatrixSaveAction.class);
        // Find the desired user roles to use in associations with security tokens.
        final UserRoleCo userRoleCo = co(UserRole.class);
        final UserRole test_role_1 = userRoleCo.findByKey(TEST_ROLE_1);
        final UserRole test_role_2 = userRoleCo.findByKey(TEST_ROLE_2);
        final UserRole test_role_3 = userRoleCo.findByKey(TEST_ROLE_3);
        final UserRole test_role_4 = userRoleCo.findByKey(TEST_ROLE_4);
        // Configure the associations to be added and those to be removed.
        final var associationsToSave = new HashMap<String, List<Integer>>();
        associationsToSave.put(SecondLevelSecurityToken1.class.getName(), List.of(test_role_3.getId().intValue()));
        associationsToSave.put(SecondLevelSecurityToken2.class.getName(), List.of(test_role_4.getId().intValue()));
        final var associationsToRemove = new HashMap<String, List<Integer>>();
        associationsToRemove.put(FirstLevelSecurityToken1.class.getName(), List.of(test_role_1.getId().intValue()));
        associationsToRemove.put(FirstLevelSecurityToken2.class.getName(), List.of(test_role_2.getId().intValue()));
        // Save the configured associations using SecurityMatrixSaveAction.
        save(securityMatrixSaveCo.new_()
                .setAssociationsToSave(associationsToSave)
                .setAssociationsToRemove(associationsToRemove));
        // Fetch Security Matrix using SecurityMatrixInsertionPoint.
        SecurityMatrixInsertionPoint securityMatrix = save(co(SecurityMatrixInsertionPoint.class).new_());
        // Verify the Security Matrix that the user will see.
        assertNull(securityMatrix.getTokenRoleMap().get(FirstLevelSecurityToken1.class.getName()));
        assertNull(securityMatrix.getTokenRoleMap().get(FirstLevelSecurityToken2.class.getName()));
        assertEquals(securityMatrix.getTokenRoleMap().get(SecondLevelSecurityToken1.class.getName()), List.of(test_role_3.getId()));
        assertEquals(securityMatrix.getTokenRoleMap().get(SecondLevelSecurityToken2.class.getName()), List.of(test_role_4.getId()));
    }

    @Test
    public void removing_view_access_tokens_throws_exception() {
        final SecurityMatrixSaveActionCo securityMatrixSaveCo = co(SecurityMatrixSaveAction.class);
        final UserRoleCo userRoleCo = co(UserRole.class);
        final UserRole admin_role = userRoleCo.findByKey(UNIT_TEST_ROLE);

        final var securityMatrixSaveAction = securityMatrixSaveCo.new_()
                .setAssociationsToRemove(Map.of(SecurityRoleAssociation_CanRead_Token.class.getName(), List.of(admin_role.getId().intValue())));
        assertThatThrownBy(() -> save(securityMatrixSaveAction))
                .hasMessage(ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_READING);
    }

    @Test
    public void removing_save_tokens_throws_exception() {
        final SecurityMatrixSaveActionCo securityMatrixSaveCo = co(SecurityMatrixSaveAction.class);
        final UserRoleCo userRoleCo = co(UserRole.class);
        final UserRole admin_role = userRoleCo.findByKey(UNIT_TEST_ROLE);

        final var securityMatrixSaveAction = securityMatrixSaveCo.new_()
                .setAssociationsToRemove(Map.of(SecurityRoleAssociation_CanSave_Token.class.getName(), List.of(admin_role.getId().intValue())));
        assertThatThrownBy(() -> save(securityMatrixSaveAction))
                .hasMessage(ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_SAVING);
    }

    @Test
    public void removing_read_and_save_tokens_throws_exception() {
        final SecurityMatrixSaveActionCo securityMatrixSaveCo = co(SecurityMatrixSaveAction.class);
        final UserRoleCo userRoleCo = co(UserRole.class);
        final UserRole admin_role = userRoleCo.findByKey(UNIT_TEST_ROLE);
        // Configure the association to remove.

        final var securityMatrixSaveAction = securityMatrixSaveCo.new_()
                .setAssociationsToRemove(Map.of(SecurityRoleAssociation_CanRead_Token.class.getName(), List.of(admin_role.getId().intValue()),
                                                SecurityRoleAssociation_CanSave_Token.class.getName(), List.of(admin_role.getId().intValue())));
        assertThatThrownBy(() -> save(securityMatrixSaveAction))
                .hasMessage(ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_READING + "<br>" + ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_SAVING);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UserRole test_role_1 = save(new_(UserRole.class, TEST_ROLE_1, "Test role 1").setActive(true));
        final UserRole test_role_2 = save(new_(UserRole.class, TEST_ROLE_2, "Test role 2").setActive(true));
        save(new_(UserRole.class, TEST_ROLE_3, "Test role 3").setActive(true));
        save(new_(UserRole.class, TEST_ROLE_4, "Test role 4").setActive(true));

        final SecurityRoleAssociationCo coSecurityRoleAssociation$ = co$(SecurityRoleAssociation.class);
        coSecurityRoleAssociation$.addAssociations(List.of(
                coSecurityRoleAssociation$.new_().setRole(test_role_1).setSecurityToken(FirstLevelSecurityToken1.class),
                coSecurityRoleAssociation$.new_().setRole(test_role_2).setSecurityToken(FirstLevelSecurityToken2.class)
                ));

        final UserRole admin = co(UserRole.class).findByKey(UNIT_TEST_ROLE);
        coSecurityRoleAssociation$.removeAssociations(List.of(
                coSecurityRoleAssociation$.new_().setRole(admin).setSecurityToken(FirstLevelSecurityToken1.class),
                coSecurityRoleAssociation$.new_().setRole(admin).setSecurityToken(FirstLevelSecurityToken2.class),
                coSecurityRoleAssociation$.new_().setRole(admin).setSecurityToken(SecondLevelSecurityToken1.class),
                coSecurityRoleAssociation$.new_().setRole(admin).setSecurityToken(SecondLevelSecurityToken2.class)
        ));
    }
}
