package ua.com.fielden.platform.security;

import ua.com.fielden.platform.entity.AbstractBatchAction;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.AssociationAction;

/**
 * Represents the user and role association batch action.
 * 
 * @author TG Team
 * 
 */
@KeyType(UserAndRoleAssociation.class)
@KeyTitle(value = "User and role association", desc = "Wraps the batch action for user and role association entity")
@CompanionObject(IUserAndRoleAssociationBatchAction.class)
@AssociationAction(firstPropertyInAssociation = "user", secondPropertyInAssociation = "userRole")
public class UserAndRoleAssociationBatchAction extends AbstractBatchAction<UserAndRoleAssociation> {

    private static final long serialVersionUID = -2896556682494734881L;

}