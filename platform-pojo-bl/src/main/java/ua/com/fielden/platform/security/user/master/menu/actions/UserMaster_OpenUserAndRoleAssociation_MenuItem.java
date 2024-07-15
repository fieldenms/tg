package ua.com.fielden.platform.security.user.master.menu.actions;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.Pair;

/**
 * Master entity object to model the detail menu item of the compound master entity object.
 *
 * @author TG Team
 *
 */
@KeyType(User.class)
@CompanionObject(UserMaster_OpenUserAndRoleAssociation_MenuItemCo.class)
@EntityTitle("User Master Roles Menu Item")
public class UserMaster_OpenUserAndRoleAssociation_MenuItem extends AbstractFunctionalEntityForCompoundMenuItem<User> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(UserMaster_OpenUserAndRoleAssociation_MenuItem.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

}
