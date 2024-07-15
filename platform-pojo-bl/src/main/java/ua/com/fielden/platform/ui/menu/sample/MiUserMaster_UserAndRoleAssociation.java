package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
/**
 * Main menu item representing an entity centre for {@link UserAndRoleAssociation}.
 *
 * @author TG Team
 *
 */
@EntityType(UserAndRoleAssociation.class)
public class MiUserMaster_UserAndRoleAssociation extends MiWithConfigurationSupport<UserAndRoleAssociation> {

}
