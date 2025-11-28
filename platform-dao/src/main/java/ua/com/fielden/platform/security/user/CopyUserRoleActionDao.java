package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.CopyUserRoleAction_CanExecute_Token;

import static ua.com.fielden.platform.error.Result.failure;

@EntityType(CopyUserRoleAction.class)
public class CopyUserRoleActionDao extends CommonEntityDao<CopyUserRoleAction> implements CopyUserRoleActionCo {

    @Override
    @SessionRequired
    @Authorise(CopyUserRoleAction_CanExecute_Token.class)
    public CopyUserRoleAction save(final CopyUserRoleAction action) {
        if (action.getSelectedIds().isEmpty()) {
            throw failure("Please select at least one %s and try again.".formatted(UserRole.ENTITY_TITLE));
        }

        return super.save(action);
    }

}
