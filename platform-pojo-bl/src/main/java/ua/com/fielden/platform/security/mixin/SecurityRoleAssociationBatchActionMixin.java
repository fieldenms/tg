package ua.com.fielden.platform.security.mixin;

import ua.com.fielden.platform.security.ISecurityRoleAssociationBatchAction;

/**
 * Mixin implementation for companion object {@link ISecurityRoleAssociationBatchAction}.
 * 
 * @author Developers
 * 
 */
public class SecurityRoleAssociationBatchActionMixin {

    private final ISecurityRoleAssociationBatchAction companion;

    public SecurityRoleAssociationBatchActionMixin(final ISecurityRoleAssociationBatchAction companion) {
        this.companion = companion;
    }

}