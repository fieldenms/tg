package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * Main menu item representing user role centre used for querying, editing and creation of new {@link UserRole}'s.
 * 
 * @author TG Team
 * 
 */
@EntityType(UserRole.class)
public class MiUserRole extends MiWithConfigurationSupport<UserRole> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "User Roles";
    private static final String description = "<html>" + "<h3>User Role Centre</h3>" + //
            "A facility to query user roles based on a number of different criteria. " + //
            "Supports opening of retrieved user roles for editing and creation of new user roles." + "</html>";
}
