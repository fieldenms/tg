package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;

/**
 * A DAO/RAO controller contract for {@link MainMenuItemInvisibility}.
 *
 * @author TG Team
 *
 */
public interface IMainMenuItemInvisibilityController2 extends IEntityDao2<MainMenuItemInvisibility> {

    /**
     * Should make the provided menu item invisible for the specified user.
     *
     * @param menuItem
     */
    void makeInvisible(MainMenuItem menuItem, User user);

    /**
     * Should make the provided menu item visible for the specified user.
     *
     * @param menuItem
     */
    void makeVisible(MainMenuItem menuItem, User user);
}

