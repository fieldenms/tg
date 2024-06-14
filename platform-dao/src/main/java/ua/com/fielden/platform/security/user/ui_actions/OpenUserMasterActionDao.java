package ua.com.fielden.platform.security.user.ui_actions;

import static ua.com.fielden.platform.security.user.ui_actions.OpenUserMasterAction.ROLES;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.AbstractOpenCompoundMasterDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

/**
 * DAO implementation for companion object {@link OpenUserMasterActionCo}.
 *
 * @author TG Team
 *
 */
@EntityType(OpenUserMasterAction.class)
public class OpenUserMasterActionDao extends AbstractOpenCompoundMasterDao<OpenUserMasterAction> implements OpenUserMasterActionCo {

    @Inject
    public OpenUserMasterActionDao(final IFilter filter, final IEntityAggregatesOperations coAggregates) {
        super(filter, coAggregates);
        addViewBinding(ROLES, UserAndRoleAssociation.class, "user");
    }

    @Override
    protected IFetchProvider<OpenUserMasterAction> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}