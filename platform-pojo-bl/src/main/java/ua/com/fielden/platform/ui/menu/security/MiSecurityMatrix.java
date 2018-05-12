package ua.com.fielden.platform.ui.menu.security;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * Main menu item representing centre with security matrix insertion point.
 *
 * @author TG Team
 *
 */
@EntityType(UserRole.class)
public class MiSecurityMatrix extends MiWithConfigurationSupport<UserRole> {

}
