package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(User.class)
public class MiUser extends MiWithConfigurationSupport<User> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "User";
    private static final String description = "<html>" + "<h3>User Centre</h3>"
            + "A facility to query User information.</html>";
}