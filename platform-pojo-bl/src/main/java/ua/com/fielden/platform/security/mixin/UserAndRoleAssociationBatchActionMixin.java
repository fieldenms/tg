package ua.com.fielden.platform.security.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;

/**
 * Mixin implementation for companion object {@link IUserAndRoleAssociationBatchAction}.
 * 
 * @author Developers
 * 
 */
public class UserAndRoleAssociationBatchActionMixin {

    private final IUserAndRoleAssociationBatchAction companion;

    public UserAndRoleAssociationBatchActionMixin(final IUserAndRoleAssociationBatchAction companion) {
        this.companion = companion;
    }

}