package ua.com.fielden.platform.entity;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.junit.Assert;
import org.junit.Test;
import ua.com.fielden.platform.data.IDomainDrivenData;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.security.provider.SecurityTestIocModule;
import ua.com.fielden.platform.security.provider.SecurityTokenNodeTransformations;
import ua.com.fielden.platform.security.tokens.FirstLevelSecurityToken1;
import ua.com.fielden.platform.security.tokens.FirstLevelSecurityToken2;
import ua.com.fielden.platform.security.tokens.SecondLevelSecurityToken1;
import ua.com.fielden.platform.security.tokens.SecondLevelSecurityToken2;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRoleCo;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.test_config.AbstractDaoTestCase.UNIT_TEST_ROLE;

public class SecurityMatrixSaveActionTest extends AbstractDaoTestCase {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory())
            .add(new SecurityTestIocModule())
            .add(new AbstractModule() {
                @Override protected void configure() {
                    bindConstant().annotatedWith(Names.named("tokens.path")).to("target/test-classes");
                    bindConstant().annotatedWith(Names.named("tokens.package")).to("ua.com.fielden.platform.security.provider");
                }
            })
            .getInjector();

    @Test
    public void save_of_security_matrix_should_deactivate_some_of_the_association_and_activate_other_associations() {
        final SecurityMatrixSaveActionCo securityMatrixSaveCo = co(SecurityMatrixSaveAction.class);

        final UserRoleCo userRoleCo = co(UserRole.class);
        final UserRole test_role_1 = userRoleCo.findByKey("UNIT_TEST_ROLE_1");
        final UserRole test_role_2 = userRoleCo.findByKey("UNIT_TEST_ROLE_2");
        final UserRole test_role_3 = userRoleCo.findByKey("UNIT_TEST_ROLE_3");
        final UserRole test_role_4 = userRoleCo.findByKey("UNIT_TEST_ROLE_4");

        final var associationsToSave = new HashMap<String, List<Integer>>();
        associationsToSave.put(SecondLevelSecurityToken1.class.getName(), List.of(test_role_3.getId().intValue()));
        associationsToSave.put(SecondLevelSecurityToken2.class.getName(), List.of(test_role_4.getId().intValue()));

        final var associationsToRemove = new HashMap<String, List<Integer>>();
        associationsToRemove.put(FirstLevelSecurityToken1.class.getName(), List.of(test_role_1.getId().intValue()));
        associationsToRemove.put(FirstLevelSecurityToken2.class.getName(), List.of(test_role_2.getId().intValue()));

        save(securityMatrixSaveCo.new_()
                .setAssociationsToSave(associationsToSave)
                .setAssociationsToRemove(associationsToRemove));

        SecurityMatrixInsertionPoint securityMatrix = save(co(SecurityMatrixInsertionPoint.class).new_());
        assertNull(securityMatrix.getTokenRoleMap().get(FirstLevelSecurityToken1.class.getName()));
        assertNull(securityMatrix.getTokenRoleMap().get(FirstLevelSecurityToken2.class.getName()));

        assertEquals(securityMatrix.getTokenRoleMap().get(SecondLevelSecurityToken1.class.getName()), List.of(test_role_3.getId()));
        assertEquals(securityMatrix.getTokenRoleMap().get(SecondLevelSecurityToken2.class.getName()), List.of(test_role_4.getId()));


    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UserRole test_role_1 = save(new_(UserRole.class, "UNIT_TEST_ROLE_1", "Test role 1").setActive(true));
        final UserRole test_role_2 = save(new_(UserRole.class, "UNIT_TEST_ROLE_2", "Test role 2").setActive(true));
        save(new_(UserRole.class, "UNIT_TEST_ROLE_3", "Test role 3").setActive(true));
        save(new_(UserRole.class, "UNIT_TEST_ROLE_4", "Test role 4").setActive(true));

        final SecurityRoleAssociationCo coSecurityRoleAssociation = co(SecurityRoleAssociation.class);
        coSecurityRoleAssociation.addAssociations(Stream.of(
                coSecurityRoleAssociation.new_().setRole(test_role_1).setSecurityToken(FirstLevelSecurityToken1.class),
                coSecurityRoleAssociation.new_().setRole(test_role_2).setSecurityToken(FirstLevelSecurityToken2.class)
                ));

        final UserRole admin = co(UserRole.class).findByKey(UNIT_TEST_ROLE);
        coSecurityRoleAssociation.removeAssociations(List.of(
                coSecurityRoleAssociation.new_().setRole(admin).setSecurityToken(FirstLevelSecurityToken1.class),
                coSecurityRoleAssociation.new_().setRole(admin).setSecurityToken(FirstLevelSecurityToken2.class),
                coSecurityRoleAssociation.new_().setRole(admin).setSecurityToken(SecondLevelSecurityToken1.class),
                coSecurityRoleAssociation.new_().setRole(admin).setSecurityToken(SecondLevelSecurityToken2.class)
        ));
    }
}
