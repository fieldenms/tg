package ua.com.fielden.platform.security;

import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserAndRoleAssociationCo;

/**
 * DAO implementation for companion object {@link IUserAndRoleAssociationBatchAction}.
 *
 * @author TG Team
 *
 */
@EntityType(UserAndRoleAssociationBatchAction.class)
public class UserAndRoleAssociationBatchActionDao extends CommonEntityDao<UserAndRoleAssociationBatchAction> implements IUserAndRoleAssociationBatchAction {

    private final UserAndRoleAssociationCo associationDao;

    @Inject
    public UserAndRoleAssociationBatchActionDao(final UserAndRoleAssociationCo associationDao) {
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
