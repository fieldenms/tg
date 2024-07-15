package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.security.user.ReUser;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(ReUser.class)
public class MiUser extends MiWithConfigurationSupport<ReUser> {
}