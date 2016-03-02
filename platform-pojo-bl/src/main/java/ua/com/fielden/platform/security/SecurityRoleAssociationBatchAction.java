package ua.com.fielden.platform.security;

import ua.com.fielden.platform.entity.AbstractBatchAction;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.AssociationAction;

/**
 * Represents the security role association (role + security token) batch action.
 * 
 * @author TG Team
 * 
 */
@KeyType(SecurityRoleAssociation.class)
@KeyTitle(value = "Security role association", desc = "Wraps the batch action for security role association entity")
@CompanionObject(ISecurityRoleAssociationBatchAction.class)
@AssociationAction(firstPropertyInAssociation = "securityToken", secondPropertyInAssociation = "role")
public class SecurityRoleAssociationBatchAction extends AbstractBatchAction<SecurityRoleAssociation> {

    private static final long serialVersionUID = -2896556682494734881L;

}