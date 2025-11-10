package ua.com.fielden.platform.security;

import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;

/**
 * DAO implementation for companion object {@link ISecurityRoleAssociationBatchAction}.
 * 
 * @author TG Team
 * 
 */
@EntityType(SecurityRoleAssociationBatchAction.class)
public class SecurityRoleAssociationBatchActionDao extends CommonEntityDao<SecurityRoleAssociationBatchAction> implements ISecurityRoleAssociationBatchAction {

    private final SecurityRoleAssociationCo associationDao;

    @Inject
    public SecurityRoleAssociationBatchActionDao(final SecurityRoleAssociationCo associationDao, final IFilter filter) {
        super(filter);

        this.associationDao = associationDao;
    }

    @Override
    @SessionRequired
    public SecurityRoleAssociationBatchAction save(final SecurityRoleAssociationBatchAction entity) {
        associationDao.addAssociations(entity.getSaveEntities());
        associationDao.addAssociations(entity.getUpdateEntities());
        associationDao.removeAssociations(entity.getRemoveEntities());
        return entity;
    }
}