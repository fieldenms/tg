package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;

/**
 * A DAO/RAO controller contract for {@link MainMenuItemInvisibility}.
 * 
 * @author TG Team
 * 
 */
public interface IMainMenuItemInvisibility extends IEntityDao<MainMenuItemInvisibility> {

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
