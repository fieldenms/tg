package ua.com.fielden.platform.security;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.security.mixin.UserAndRoleAssociationBatchActionMixin;
import com.google.inject.Inject;

/**
 * RAO implementation for master object {@link IUserAndRoleAssociationBatchAction} based on a common with DAO mixin.
 * 
 * @author Developers
 * 
 */
@EntityType(UserAndRoleAssociationBatchAction.class)
public class UserAndRoleAssociationBatchActionRao extends CommonEntityRao<UserAndRoleAssociationBatchAction> implements IUserAndRoleAssociationBatchAction {

    private final UserAndRoleAssociationBatchActionMixin mixin;

    @Inject
    public UserAndRoleAssociationBatchActionRao(final RestClientUtil restUtil) {
        super(restUtil);

        mixin = new UserAndRoleAssociationBatchActionMixin(this);
    }

}