package ua.com.fielden.platform.security;

import com.google.inject.Inject;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.security.mixin.SecurityRoleAssociationBatchActionMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * RAO implementation for master object {@link ISecurityRoleAssociationBatchAction} based on a common with DAO mixin.
 * 
 * @author Developers
 * 
 */
@EntityType(SecurityRoleAssociationBatchAction.class)
public class SecurityRoleAssociationBatchActionRao extends CommonEntityRao<SecurityRoleAssociationBatchAction> implements ISecurityRoleAssociationBatchAction {

    private final SecurityRoleAssociationBatchActionMixin mixin;

    @Inject
    public SecurityRoleAssociationBatchActionRao(final RestClientUtil restUtil) {
        super(restUtil);

        mixin = new SecurityRoleAssociationBatchActionMixin(this);
    }

}