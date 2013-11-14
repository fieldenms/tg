package ua.com.fielden.platform.security;

import java.util.Set;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.mixin.UserAndRoleAssociationBatchActionMixin;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link IUserAndRoleAssociationBatchAction}.
 *
 * @author Developers
 *
 */
@EntityType(UserAndRoleAssociationBatchAction.class)
public class UserAndRoleAssociationBatchActionDao extends CommonEntityDao<UserAndRoleAssociationBatchAction> implements IUserAndRoleAssociationBatchAction {

    private final UserAndRoleAssociationBatchActionMixin mixin;
    private final IUserAndRoleAssociationDao associationDao;

    @Inject
    public UserAndRoleAssociationBatchActionDao(final IUserAndRoleAssociationDao associationDao, final IFilter filter) {
        super(filter);

        mixin = new UserAndRoleAssociationBatchActionMixin(this);
        this.associationDao = associationDao;
    }

    @Override
    @SessionRequired
    public UserAndRoleAssociationBatchAction save(final UserAndRoleAssociationBatchAction entity) {
	processSaveAction(entity.getSaveEntities());
	processSaveAction(entity.getUpdateEntities());
	associationDao.removeAssociation(entity.getRemoveEntities());
	return entity;
    }

    /**
     * Saves the set of given associations.
     *
     * @param associations
     */
    private void processSaveAction(final Set<UserAndRoleAssociation> associations) {
	for (final UserAndRoleAssociation association : associations) {
	    associationDao.save(association);
	}
    }


}