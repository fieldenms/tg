package ua.com.fielden.platform.security.user;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.persistent.KeyNumber_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.ImmutableSetUtils;
import ua.com.fielden.platform.web.centre.CentreContext;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.security.user.CopyUserRoleActionCo.ERR_EMPTY_SELECTION;

public class CopyUserRoleActionTest extends AbstractDaoTestCase {

    public static final String
            ROLE1 = "ROLE1",
            ROLE2 = "ROLE2";

    @Test
    public void action_cannot_be_produced_if_the_selection_is_empty() {
        assertThatThrownBy(this::produceAction)
                .hasMessage(ERR_EMPTY_SELECTION);
    }

    @Test
    public void action_cannot_be_saved_if_the_selection_is_empty() {
        final var action = new_(CopyUserRoleAction.class);
        assertThatThrownBy(() -> save(action))
                .hasMessage(ERR_EMPTY_SELECTION);
    }

    @Test
    public void if_a_single_role_with_at_least_one_token_is_selected_then_all_of_its_tokens_are_associated_with_the_new_role() {
        final var role1Tokens = Set.of(User_CanSave_Token.class, User_CanDelete_Token.class);
        addAssociations(ROLE1, role1Tokens);

        final var copy1Key = "COPY1";
        save(produceAction(ROLE1).setRoleTitle(copy1Key).setRoleDesc("Copy 1"));

        final var copy1Tokens = findAssociatedTokens(copy1Key);
        assertThat(copy1Tokens).isEqualTo(role1Tokens);
    }

    @Test
    public void if_a_single_role_without_tokens_is_selected_then_no_tokens_are_associated_with_the_new_role() {
        final var role1Tokens = findAssociatedTokens(ROLE1);
        assertThat(role1Tokens).isEmpty();

        final var copy1Key = "COPY1";
        save(produceAction(ROLE1).setRoleTitle(copy1Key).setRoleDesc("Copy 1"));

        final var copy1Tokens = findAssociatedTokens(copy1Key);
        assertThat(copy1Tokens).isEmpty();
    }

    @Test
    public void if_two_roles_are_selected_then_a_union_of_their_tokens_is_associated_with_the_new_role() {
        final var role1Tokens = Set.of(User_CanSave_Token.class, User_CanDelete_Token.class, User_CanReadModel_Token.class);
        addAssociations(ROLE1, role1Tokens);
        final var role2Tokens = Set.of(User_CanSave_Token.class, UserAndRoleAssociation_CanRead_Token.class, User_CanReadModel_Token.class);
        addAssociations(ROLE2, role2Tokens);

        final var copy1Key = "COPY1";
        save(produceAction(ROLE1, ROLE2).setRoleTitle(copy1Key).setRoleDesc("Copy 1"));

        final var copy1Tokens = findAssociatedTokens(copy1Key);
        assertThat(copy1Tokens).isEqualTo(ImmutableSetUtils.union(role1Tokens, role2Tokens));
    }

    @Test
    public void only_existing_associations_are_copied() {
        addAssociations(ROLE1, Set.of(User_CanSave_Token.class, User_CanDelete_Token.class, User_CanReadModel_Token.class, KeyNumber_CanRead_Token.class));
        removeAssociations(ROLE1, Set.of(User_CanSave_Token.class, KeyNumber_CanRead_Token.class));

        addAssociations(ROLE2, Set.of(User_CanSave_Token.class, UserAndRoleAssociation_CanRead_Token.class, User_CanReadModel_Token.class,
                                      CopyUserRoleAction_CanExecute_Token.class));
        removeAssociations(ROLE2, Set.of(User_CanReadModel_Token.class, CopyUserRoleAction_CanExecute_Token.class));

        final var copy1Key = "COPY1";
        save(produceAction(ROLE1, ROLE2).setRoleTitle(copy1Key).setRoleDesc("Copy 1"));

        final var copy1Tokens = findAssociatedTokens(copy1Key);
        assertThat(copy1Tokens)
                .isEqualTo(Set.of(User_CanSave_Token.class, User_CanDelete_Token.class, User_CanReadModel_Token.class, UserAndRoleAssociation_CanRead_Token.class));
    }

    @Test
    public void new_role_is_created_with_the_specified_property_values() {
        final var roleTitle = "COPY1";
        final var roleDesc = "Copy 1";
        final var roleActive = false;
        save(produceAction(ROLE1).setRoleTitle(roleTitle).setRoleDesc(roleDesc).setRoleActive(roleActive));

        assertThat(co(UserRole.class).findByKey(roleTitle))
                .isNotNull()
                .satisfies(role -> assertThat(role.getKey()).isEqualTo(roleTitle))
                .satisfies(role -> assertThat(role.getDesc()).isEqualTo(roleDesc))
                .satisfies(role -> assertThat(role.isActive()).isEqualTo(roleActive));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UserRole role1 = save(new_(UserRole.class, ROLE1, "Role 1").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, ROLE2, "Role 2").setActive(true));
    }

    private CopyUserRoleAction produceAction(final String... roleKeys) {
        final var roles = roleKeys.length == 0
                ? List.<UserRole>of()
                : co(UserRole.class).getAllEntities(from(select(UserRole.class).where().prop(KEY).in().values(roleKeys).model()).model());
        return produceAction(roles);
    }

    private CopyUserRoleAction produceAction(final List<UserRole> roles) {
        return getInstance(CopyUserRoleActionProducer.class)
                .setContext(new CentreContext<UserRole, AbstractEntity<?>>().setSelectedEntities(roles))
                .newEntity();
    }

    private void addAssociations(final String userRoleKey, final Collection<Class<? extends ISecurityToken>> tokenTypes) {
        final var userRole = co(UserRole.class).findByKey(userRoleKey);
        final SecurityRoleAssociationCo co$Association = co(SecurityRoleAssociation.class);
        co$Association.addAssociations(tokenTypes.stream().map(tokenType -> co$Association.new_().setSecurityToken(tokenType).setRole(userRole)));
    }

    private void removeAssociations(final String userRoleKey, final Collection<Class<? extends ISecurityToken>> tokenTypes) {
        final var userRole = co(UserRole.class).findByKey(userRoleKey);
        final SecurityRoleAssociationCo co$Association = co(SecurityRoleAssociation.class);
        co$Association.removeAssociations(tokenTypes.stream().map(tokenType -> co$Association.new_().setSecurityToken(tokenType).setRole(userRole)).toList());
    }

    private Set<Class<? extends ISecurityToken>> findAssociatedTokens(final String roleKey) {
        return co(EntityAggregates.class).getAllEntities(
                        from(select(SecurityRoleAssociation.class).where()
                                     .prop("role.key").eq().val(roleKey)
                                     .yield().prop("securityToken").as("token")
                                     .modelAsAggregate())
                        .model())
                .stream()
                .map(agg -> agg.<Class<? extends ISecurityToken>>get("token"))
                .collect(toSet());
    }

}
