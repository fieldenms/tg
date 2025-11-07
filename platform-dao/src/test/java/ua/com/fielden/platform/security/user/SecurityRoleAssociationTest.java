package ua.com.fielden.platform.security.user;

import org.junit.Test;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanSave_Token;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.utils.CollectionUtil.concatList;

public class SecurityRoleAssociationTest extends AbstractDaoTestCase {

    @Test
    public void addAssociations_creates_records_for_all_specified_assocations() {
        final var userRole = save(new_(UserRole.class, "TEST_ROLE_01", "Test role 01"));
        final SecurityRoleAssociationCo coSecurityRoleAssociation = co(SecurityRoleAssociation.class);

        final var tokens1 = List.of(User_CanSave_Token.class, User_CanRead_Token.class);
        coSecurityRoleAssociation.addAssociations(tokens1.stream().map(tok -> new_(SecurityRoleAssociation.class).setRole(userRole).setSecurityToken(tok)).collect(toList()));
        assertThat(tokensForRole(userRole)).containsExactlyInAnyOrderElementsOf(tokens1);

        final var tokens2 = List.of(Attachment_CanRead_Token.class);
        coSecurityRoleAssociation.addAssociations(tokens2.stream().map(tok -> new_(SecurityRoleAssociation.class).setRole(userRole).setSecurityToken(tok)).collect(toList()));
        assertThat(tokensForRole(userRole)).containsExactlyInAnyOrderElementsOf(concatList(tokens1, tokens2));

        coSecurityRoleAssociation.addAssociations(new ArrayList<>());

        assertThat(tokensForRole(userRole)).containsExactlyInAnyOrderElementsOf(concatList(tokens1, tokens2));
    }

    private List<Class<? extends ISecurityToken>> tokensForRole(final UserRole role) {
        return co(SecurityRoleAssociation.class).getAllEntities(
                from(select(SecurityRoleAssociation.class).where()
                             .prop("role").eq().val(role)
                             .model())
                .with(fetchIdOnly(SecurityRoleAssociation.class).with("securityToken"))
                .model())
                .stream()
                .<Class<? extends ISecurityToken>>map(SecurityRoleAssociation::getSecurityToken)
                .toList();
    }

}
