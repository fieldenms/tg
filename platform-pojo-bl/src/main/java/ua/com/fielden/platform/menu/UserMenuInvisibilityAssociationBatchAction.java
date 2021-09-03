package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.AbstractBatchAction;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity_centre.review.annotations.AssociationAction;

/**
 * Represents the user and invisible menu item association batch action.
 *
 * @author TG Team
 *
 */
@KeyType(WebMenuItemInvisibility.class)
@KeyTitle(value = "User and invisible menu item association", desc = "Wraps the batch action for user and invisible menu item association entity")
@CompanionObject(UserMenuInvisibilityAssociationBatchActionCo.class)
@AssociationAction(firstPropertyInAssociation = "owner", secondPropertyInAssociation = "menuItemUri")
public class UserMenuInvisibilityAssociationBatchAction extends AbstractBatchAction<WebMenuItemInvisibility> {

}
