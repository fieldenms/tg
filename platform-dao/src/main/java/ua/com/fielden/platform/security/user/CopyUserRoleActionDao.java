package ua.com.fielden.platform.security.user;

import jakarta.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;

import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.error.Result.failure;

@EntityType(CopyUserRoleAction.class)
public class CopyUserRoleActionDao extends CommonEntityDao<CopyUserRoleAction> implements CopyUserRoleActionCo {

    private final IAuthorisationModel authModel;

    @Inject
    protected CopyUserRoleActionDao(final IAuthorisationModel authModel) {
        this.authModel = authModel;
    }

    @Override
    @SessionRequired
    @Authorise(UserRole_CanSave_Token.class)
    public CopyUserRoleAction save(final CopyUserRoleAction action) {
        // `@Authorise` does not support multiple tokens, so authorise imperatively.
        authModel.authorise(SecurityRoleAssociation_CanSave_Token.class).ifFailure(Result::throwRuntime);

        if (action.getSelectedIds().isEmpty()) {
            throw failure(ERR_EMPTY_SELECTION);
        }

        action.isValid().ifFailure(Result::throwRuntime);

        final var co$UserRole = co$(UserRole.class);
        final var savedRole = co$UserRole.save(
                co$UserRole.new_()
                        .setKey(action.getRoleTitle())
                        .setDesc(action.getRoleDesc())
                        .setActive(action.isRoleActive()));

        final var qAssociations = select(SecurityRoleAssociation.class)
                .where()
                .prop(SecurityRoleAssociation.ROLE).in().values(action.getSelectedIds())
                .and()
                .prop(ACTIVE).eq().val(true)
                .groupBy().prop(SecurityRoleAssociation.SECURITY_TOKEN)
                .yield().prop(SecurityRoleAssociation.SECURITY_TOKEN).as(SecurityRoleAssociation.SECURITY_TOKEN)
                .modelAsEntity(SecurityRoleAssociation.class);

        final SecurityRoleAssociationCo co$Association = co$(SecurityRoleAssociation.class);
        final var associations = co$Association.getAllEntities(from(qAssociations).with(fetch(SecurityRoleAssociation.class)).model())
                .stream()
                // We can reuse retrieved instances, as `addAssociations` looks only at key values.
                .map(assoc$ -> assoc$.setRole(savedRole))
                .toList();
        co$Association.addAssociations(associations);

        return super.save(action);
    }

}
