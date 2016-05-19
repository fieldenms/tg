package ua.com.fielden.platform.security;

import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociation;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.mixin.SecurityRoleAssociationBatchActionMixin;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * DAO implementation for companion object {@link ISecurityRoleAssociationBatchAction}.
 * 
 * @author Developers
 * 
 */
@EntityType(SecurityRoleAssociationBatchAction.class)
public class SecurityRoleAssociationBatchActionDao extends CommonEntityDao<SecurityRoleAssociationBatchAction> implements ISecurityRoleAssociationBatchAction {

    private final SecurityRoleAssociationBatchActionMixin mixin;
    private final ISecurityRoleAssociation associationDao;

    @Inject
    public SecurityRoleAssociationBatchActionDao(final ISecurityRoleAssociation associationDao, final IFilter filter) {
        super(filter);

        mixin = new SecurityRoleAssociationBatchActionMixin(this);
        this.associationDao = associationDao;
    }

    @Override
    @SessionRequired
    public SecurityRoleAssociationBatchAction save(final SecurityRoleAssociationBatchAction entity) {
        processSaveAction(entity.getSaveEntities());
        processSaveAction(entity.getUpdateEntities());
        associationDao.removeAssociations(entity.getRemoveEntities());
        return entity;
    }

    /**
     * Saves the set of given associations.
     * 
     * @param associations
     */
    private void processSaveAction(final Set<SecurityRoleAssociation> associations) {
        for (final SecurityRoleAssociation association : associations) {
            associationDao.save(association);
        }
    }

}