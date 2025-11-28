package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.CopyUserRoleAction_CanExecute_Token;
import ua.com.fielden.platform.utils.StreamUtils;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.error.Result.failure;

@EntityType(CopyUserRoleAction.class)
public class CopyUserRoleActionDao extends CommonEntityDao<CopyUserRoleAction> implements CopyUserRoleActionCo {

    @Override
    @SessionRequired
    @Authorise(CopyUserRoleAction_CanExecute_Token.class)
    public CopyUserRoleAction save(final CopyUserRoleAction action) {
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
                .prop("role").in().values(action.getSelectedIds())
                // TODO #2109 Uncomment once #2444 is merged.
                // .and()
                // .prop("active").eq().val(true)
                .model();

        final SecurityRoleAssociationCo co$Association = co$(SecurityRoleAssociation.class);
        try (final var stream = co$Association.stream(from(qAssociations).with(fetchNone(SecurityRoleAssociation.class).with("securityToken")).lightweight().model(), 1000)) {
            co$Association.addAssociations(
                    StreamUtils.distinct(stream, SecurityRoleAssociation::getSecurityToken)
                               .map(assoc -> co$Association.new_().setRole(savedRole).setSecurityToken(assoc.getSecurityToken())));
        }

        return super.save(action);
    }

}
